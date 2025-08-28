package com.example.chat;

import com.example.chat.dto.ChatDtos;
import com.example.chat.dto.ChatDtos.ChatRequest;
import com.example.chat.dto.ChatDtos.ChatResponse;
import com.example.domain.chat.ChatMessage;
import com.example.domain.user.User;
import com.example.repository.ChatMessageRepository;
import com.example.repository.ConversationRepository;
import com.example.search.SerperClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final String SYSTEM_PROMPT = """
Aşağıdaki öğrenci sorusu/sohbeti için SADECE tek bir JSON NESNESİ üret.
ŞEMA:
{
  "response": "kullanıcıya verilecek açıklayıcı ve net yanıt",
  "status": "durumu veya etiket",
  "topic": "kısa konu etiketi (örn: organic-chemistry)",
  "difficulty": "başlangıç | orta | ileri",
  "next_step": "öğrenci için uygulanabilir bir sonraki adım",
  "confidence": 0.0,
  "language": "tr | en | ...",
  "needs_resources": true | false,
  "recommended_resources": [
    {"title": "kaynak adı", "url": "https://..."}
  ]
}
Kurallar:
- Yalnızca YUKARIDAKİ alan adlarıyla, geçerli JSON döndür.
- Ek açıklama / markdown YAZMA.
- "response" kısa ve seviyeye uygun olsun.
- "topic" tek kısa etiket olsun.
- "needs_resources": Kullanıcı açıkça kaynak/link isterse veya soru öğrenme/araştırma/öğretici nitelikteyse TRUE; selamlaşma, small-talk gibi bilgi içermeyen mesajlarda FALSE.
- "recommended_resources": needs_resources TRUE ise 1–3 adet, aksi halde boş dizi.
- Yanıt dili kullanıcının diline uyumlu olsun (TR/EN).
""";

    private static final int HISTORY_WINDOW = 12;        // model bağlamına koyacağımız max mesaj
    private static final int PART_CHAR_LIMIT = 3000;     // her parça için uzunluk sınırı

    private final ChatMessageRepository repo;
    private final ConversationRepository convRepo;
    private final SerperClient serper;
    private final WebClient geminiWeb;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${ai.provider:gemini}") private String provider;
    @Value("${gemini.api.key:}") private String geminiApiKey;
    @Value("${gemini.model:gemini-1.5-flash}") private String geminiModel;
    @Value("${search.topN:3}") private int topN;

    public ChatService(ChatMessageRepository repo, SerperClient serper, ConversationRepository convRepo) {
        this.repo = repo;
        this.serper = serper;
        this.convRepo = convRepo;
        this.geminiWeb = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    // --------- Conversation yardımcıları ----------
    public Long createConversation(Long userId, String title) {
        var c = new com.example.domain.chat.Conversation(userId,
                (title == null || title.isBlank()) ? "Yeni sohbet" : title.trim());
        c = convRepo.save(c);
        return c.getId();
    }

    private String autoTitleFrom(String userMsg, String topic, String response) {
        if (topic != null && !topic.isBlank()) return topic;
        String base = (userMsg != null && !userMsg.isBlank()) ? userMsg : response;
        if (base == null) return "Yeni sohbet";
        base = base.trim().replaceAll("\\s+"," ");
        return base.length() > 60 ? base.substring(0,60) + "…" : base;
    }

    // Mesaj atma
    public ChatResponse ask(User user, ChatRequest req) {
        String userMessage = req.getMessage();

        // 1) Sohbeti belirle/oluştur
        Long conversationId = req.getConversationId();
        if (conversationId == null) {
            conversationId = createConversation(user.getId(), req.getTitle());
        } else {
            convRepo.findByIdAndUserId(conversationId, user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        }

        // 2) Bu sohbetin son N mesajıyla bağlamı hazırla
        List<Map<String, Object>> contents =
                buildContentsWithHistory(conversationId, user.getId(), userMessage);

        // 3) Modeli çağır
        String rawModelText = "gemini".equalsIgnoreCase(provider)
                ? callGemini(contents)
                : "{\"response\":\"stub\",\"status\":\"neutral\",\"topic\":\"\",\"difficulty\":\"\",\"next_step\":\"\",\"confidence\":0.7,\"language\":\"tr\",\"needs_resources\":false,\"recommended_resources\":[]}";

        ParsedResult pr = parseModelOutput(rawModelText);

        boolean explicitAsk = isExplicitResourceAsk(userMessage);
        Boolean modelNeeds = pr.needsResources;
        boolean inferredLearning = isLearningOriented(userMessage);
        boolean needsResources = explicitAsk || (modelNeeds != null ? modelNeeds : inferredLearning);

        List<ChatDtos.ResourceLink> resources = new ArrayList<>();
        if (needsResources) {
            if (pr.resourcesJson != null && pr.resourcesJson.isArray() && pr.resourcesJson.size() > 0) {
                resources = toResourceList(pr.resourcesJson);
            }
            if (resources.isEmpty() && serper.isEnabled()) {
                resources = findResourcesWithSerper(pr.topic, userMessage);
            }
            if (resources.isEmpty()) {
                resources = genericFallback();
            }
        }

        pr.status     = clip(pr.status, 100);
        pr.topic      = clip(pr.topic, 100);
        pr.difficulty = clip(pr.difficulty, 30);
        pr.language   = clip(pr.language, 10);

        // 4) Kaydet
        ChatMessage saved = new ChatMessage(
                user.getId(),
                userMessage,
                pr.response != null ? pr.response : rawModelText,
                Instant.now(),
                pr.status,
                pr.topic,
                pr.nextStep,
                pr.confidence,
                pr.language,
                pr.difficulty,
                mapper.valueToTree(resources)
        );
        saved.setConversationId(conversationId); // <<< sohbet bağı
        saved = repo.save(saved);

        // 5) Conversation'ı güncelle (başlık + updatedAt)
        convRepo.findById(conversationId).ifPresent(c -> {
            c.setUpdatedAt(Instant.now());
            if (c.getTitle() == null || c.getTitle().isBlank() || "Yeni sohbet".equalsIgnoreCase(c.getTitle())) {
                c.setTitle(autoTitleFrom(userMessage, pr.topic, pr.response));
            }
            convRepo.save(c);
        });

        return new ChatResponse(
                saved.getId(),
                saved.getUserId(),
                saved.getRequest(),
                saved.getReply(),
                saved.getStatus(),
                saved.getTopic(),
                saved.getDifficulty(),
                saved.getNextStep(),
                saved.getConfidence(),
                saved.getCreatedAt(),
                "Gemini",
                resources
        );
    }

    // Listeleme

    public List<ChatDtos.ChatResponse> history(Long userId, int limit) {
        var page = PageRequest.of(0, Math.max(1, Math.min(limit, 200)));
        var rows = repo.findByUserIdOrderByCreatedAtDesc(userId, page);
        List<ChatDtos.ChatResponse> out = new ArrayList<>();
        for (ChatMessage m : rows) {
            out.add(new ChatDtos.ChatResponse(
                    m.getId(), m.getUserId(), m.getRequest(), m.getReply(),
                    m.getStatus(), m.getTopic(), m.getDifficulty(), m.getNextStep(),
                    m.getConfidence(), m.getCreatedAt(), "Gemini", toResourceList(m.getResourcesJson())
            ));
        }
        return out;
    }


    public List<ChatDtos.ConversationSummary> listConversations(Long userId) {
        var convs = convRepo.findByUserIdOrderByUpdatedAtDesc(userId);
        List<ChatDtos.ConversationSummary> out = new ArrayList<>();
        for (var c : convs) {
            var last = repo.findByConversationIdOrderByCreatedAtDesc(
                    c.getId(), PageRequest.of(0, 1));
            String snippet = last.isEmpty()
                    ? ""
                    : Optional.ofNullable(last.get(0).getRequest()).orElse(last.get(0).getReply());
            if (snippet == null) snippet = "";
            snippet = snippet.replaceAll("\\s+"," ").trim();
            if (snippet.length() > 120) snippet = snippet.substring(0,120) + "…";
            int count = (int) repo.countByConversationId(c.getId());
            out.add(new ChatDtos.ConversationSummary(
                    c.getId(), c.getTitle(), snippet, count, c.getUpdatedAt()
            ));
        }
        return out;
    }


    public List<ChatDtos.ChatResponse> conversationMessages(Long userId, Long conversationId, int limit) {
        convRepo.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        var rows = repo.findByConversationIdOrderByCreatedAtAsc(conversationId);
        if (limit > 0 && rows.size() > limit) {
            rows = rows.subList(rows.size() - limit, rows.size());
        }
        List<ChatDtos.ChatResponse> out = new ArrayList<>();
        for (ChatMessage m : rows) {
            out.add(new ChatDtos.ChatResponse(
                    m.getId(), m.getUserId(), m.getRequest(), m.getReply(),
                    m.getStatus(), m.getTopic(), m.getDifficulty(), m.getNextStep(),
                    m.getConfidence(), m.getCreatedAt(), "Gemini", toResourceList(m.getResourcesJson())
            ));
        }
        return out;
    }

    // Model için bağlamı kur
    private List<Map<String, Object>> buildContentsWithHistory(Long conversationId, Long userId, String currentUserMessage) {
        var recent = repo.findByConversationIdOrderByCreatedAtDesc(conversationId, PageRequest.of(0, HISTORY_WINDOW));
        Collections.reverse(recent);

        List<Map<String, Object>> contents = new ArrayList<>();
        for (ChatMessage m : recent) {
            String u = safeTake(m.getRequest(), PART_CHAR_LIMIT);
            if (u != null && !u.isBlank()) {
                contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", u))));
            }
            String r = extractReplyText(m.getReply());
            r = safeTake(r, PART_CHAR_LIMIT);
            if (r != null && !r.isBlank()) {
                contents.add(Map.of("role", "model", "parts", List.of(Map.of("text", r))));
            }
        }
        contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", safeTake(currentUserMessage, PART_CHAR_LIMIT)))));
        return contents;
    }

    private String extractReplyText(String reply) {
        if (reply == null) return null;
        String s = stripFences(reply.trim());
        try {
            JsonNode n = mapper.readTree(s);
            if (n.isObject() && n.has("response")) {
                String resp = n.path("response").asText("");
                if (!resp.isBlank()) return resp;
            }
        } catch (Exception ignored) {}
        return s;
    }

    private String stripFences(String t) {
        if (t == null) return null;
        String s = t.trim();
        if (s.startsWith("```")) {
            s = s.replaceFirst("^```[a-zA-Z]*", "")
                    .replaceFirst("```$", "")
                    .trim();
        }
        return s;
    }

    private String safeTake(String s, int max) {
        if (s == null) return null;
        s = s.trim();
        return s.length() <= max ? s : s.substring(0, max);
    }

    // Model çağrısı
    private String callGemini(List<Map<String, Object>> contents) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return "{\"response\":\"[Gemini yok]\",\"status\":\"error\",\"topic\":\"\",\"difficulty\":\"\",\"next_step\":\"\",\"confidence\":0.0,\"language\":\"tr\",\"needs_resources\":false,\"recommended_resources\":[]}";
        }
        try {
            Map<String, Object> body = Map.of(
                    "systemInstruction", Map.of("role","system","parts", List.of(Map.of("text", SYSTEM_PROMPT))),
                    "contents", contents,
                    "generationConfig", Map.of("responseMimeType","application/json")
            );

            String json = geminiWeb.post()
                    .uri(u -> u.path("/v1beta/models/{model}:generateContent")
                            .queryParam("key", geminiApiKey)
                            .build(geminiModel))
                    .header("Content-Type","application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (json != null && json.contains("\"error\"")) {
                if (json.contains("\"code\":429") || json.contains("Too Many Requests")) {
                    return "{\"response\":\"Şu anda model kotası dolu.\",\"status\":\"busy\",\"topic\":\"\",\"difficulty\":\"\",\"next_step\":\"\",\"confidence\":0.0,\"language\":\"tr\",\"needs_resources\":false,\"recommended_resources\":[]}";
                }
                return "{\"response\":\"Model hatası.\",\"status\":\"error\",\"topic\":\"\",\"difficulty\":\"\",\"next_step\":\"\",\"confidence\":0.0,\"language\":\"tr\",\"needs_resources\":false,\"recommended_resources\":[]}";
            }

            JsonNode root = mapper.readTree(json);
            JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
            String text = parts.isArray() && parts.size() > 0 ? parts.get(0).path("text").asText("") : "";
            if (text.isBlank()) {
                return "{\"response\":\"Boş çıktı geldi.\",\"status\":\"error\",\"topic\":\"\",\"difficulty\":\"\",\"next_step\":\"\",\"confidence\":0.0,\"language\":\"tr\",\"needs_resources\":false,\"recommended_resources\":[]}";
            }
            return text.trim();

        } catch (Exception e) {
            return "{\"response\":\"Model çağrısı hata verdi: "+ e.getClass().getSimpleName() +".\",\"status\":\"error\",\"topic\":\"\",\"difficulty\":\"\",\"next_step\":\"\",\"confidence\":0.0,\"language\":\"tr\",\"needs_resources\":false,\"recommended_resources\":[]}";
        }
    }

    // Kaynak yardımcıları
    private List<ChatDtos.ResourceLink> toResourceList(JsonNode resourcesJson) {
        List<ChatDtos.ResourceLink> list = new ArrayList<>();
        if (resourcesJson != null && resourcesJson.isArray()) {
            for (JsonNode it : resourcesJson) {
                String t = it.path("title").asText(null);
                String u = it.path("url").asText(null);
                if (t != null && u != null && !u.isBlank()) {
                    list.add(new ChatDtos.ResourceLink(t, u));
                }
            }
        }
        return list;
    }

    private List<ChatDtos.ResourceLink> findResourcesWithSerper(String topic, String userMessage) {
        if (!serper.isEnabled()) return List.of();
        String baseQuery = (topic != null && !topic.isBlank())
                ? (topic + " " + userMessage)
                : userMessage;
        List<ChatDtos.ResourceLink> results = searchOnce(baseQuery);
        if (!results.isEmpty()) return results;
        String altQuery = baseQuery + " tutorial study guide";
        return searchOnce(altQuery);
    }

    private List<ChatDtos.ResourceLink> searchOnce(String q) {
        var links = serper.searchLinks(q);
        if (links == null) return List.of();
        return links.stream()
                .map(m -> new ChatDtos.ResourceLink(
                        m.getOrDefault("title", "Kaynak"),
                        m.getOrDefault("url", "")
                ))
                .filter(l -> l.getUrl() != null && !l.getUrl().isBlank())
                .limit(Math.max(1, topN))
                .collect(Collectors.toList());
    }

    private List<ChatDtos.ResourceLink> genericFallback() {
        List<ChatDtos.ResourceLink> f = new ArrayList<>();
        f.add(new ChatDtos.ResourceLink("OpenStax (ücretsiz ders kitapları)", "https://openstax.org/"));
        f.add(new ChatDtos.ResourceLink("MIT OpenCourseWare", "https://ocw.mit.edu/"));
        f.add(new ChatDtos.ResourceLink("OER Commons", "https://www.oercommons.org/"));
        return f.subList(0, Math.min(Math.max(1, topN), f.size()));
    }

    private boolean isExplicitResourceAsk(String msg) {
        if (msg == null) return false;
        String m = msg.toLowerCase(Locale.ROOT);
        return m.contains("kaynak") || m.contains("link") || m.contains("referans")
                || m.contains("resource") || m.contains("reference")
                || m.contains("öner") || m.contains("recommend");
    }

    private boolean isLearningOriented(String msg) {
        if (msg == null) return false;
        String m = msg.toLowerCase(Locale.ROOT);
        if (m.matches("^(merhaba|selam|naber|nasılsın|hello|hi|hey)[.!? ]*$")) return false;
        String[] keys = {
                "nasıl yapılır","nedir","öğrenmek","öğreniyorum","anlat","özetle",
                "temeli","temeller","best practice","ödev","ders","konu","teori",
                "tutorial","guide","kılavuz","study","çalışma planı","proje nasıl",
                "karşılaştır","örnek ver","adım adım","detaylı açıkla"
        };
        for (String k : keys) if (m.contains(k)) return true;
        return m.length() > 40;
    }

    // parse yardımcıları
    private ParsedResult parseModelOutput(String raw) {
        ParsedResult pr = new ParsedResult();
        if (raw == null || raw.isBlank()) return pr;
        String cleaned = stripFences(raw.trim());
        try {
            JsonNode root = mapper.readTree(cleaned);
            pr.response       = getAsText(root, "response");
            pr.status         = getAsText(root, "status");
            pr.topic          = getAsText(root, "topic");
            pr.difficulty     = getAsText(root, "difficulty");
            pr.nextStep       = getAsText(root, "next_step");
            pr.language       = getAsText(root, "language");
            if (root.has("confidence") && !root.get("confidence").isNull()) {
                try { pr.confidence = root.get("confidence").asDouble(); } catch (Exception ignored) {}
            }
            if (root.has("needs_resources") && !root.get("needs_resources").isNull()) {
                try { pr.needsResources = root.get("needs_resources").asBoolean(); } catch (Exception ignored) {}
            }
            if (root.has("recommended_resources") && root.get("recommended_resources").isArray()) {
                pr.resourcesJson = root.get("recommended_resources");
            }
            if (pr.response == null || pr.response.isBlank()) pr.response = cleaned;
        } catch (Exception e) {
            pr.response = raw;
        }
        return pr;
    }

    private String getAsText(JsonNode root, String field) {
        JsonNode n = root.path(field);
        if (n.isMissingNode() || n.isNull()) return null;
        String v = n.asText("");
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private static String clip(String v, int max) {
        if (v == null) return null;
        v = v.trim();
        return v.length() <= max ? v : v.substring(0, max);
    }

    private static class ParsedResult {
        String response;
        String status;
        String topic;
        String difficulty;
        String nextStep;
        Double confidence;
        String language;
        Boolean needsResources;
        JsonNode resourcesJson;
    }
}