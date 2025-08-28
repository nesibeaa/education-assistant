package com.example.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    private final WebClient web = WebClient.builder()
            .baseUrl("https://google.serper.dev")
            .defaultHeader("Content-Type", "application/json")
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${serper.api.key:}")
    private String serperKey;

    @Value("${search.topN:3}")
    private int topN;

    public record Result(String title, String url, String snippet) {}

    /** Basit Google araması (serper.dev) */
    public List<Result> search(String query, String langOrCountry) {
        List<Result> out = new ArrayList<>();
        if (serperKey == null || serperKey.isBlank()) return out;
        try {
            Map<String, Object> body = Map.of(
                    "q", query,
                    "gl", langOrCountry == null || langOrCountry.isBlank() ? "tr" : langOrCountry
            );

            String json = web.post()
                    .uri("/search")
                    .header("X-API-KEY", serperKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = mapper.readTree(json);
            JsonNode organic = root.path("organic");
            if (organic.isArray()) {
                int count = 0;
                for (JsonNode it : organic) {
                    String title = it.path("title").asText("");
                    String url   = it.path("link").asText("");
                    String snippet = it.path("snippet").asText("");
                    if (!title.isBlank() && !url.isBlank()) {
                        out.add(new Result(title, url, snippet));
                        if (++count >= topN) break;
                    }
                }
            }
        } catch (Exception ignored) {}
        return out;
    }

    /** Gemini’ye eklenecek kısa bağlam metni üretir */
    public String toContext(List<Result> results) {
        if (results == null || results.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("WEB_RESULTS (kısa özet ve URL'ler):\n");
        int i = 1;
        for (Result r : results) {
            sb.append(i++).append(") ").append(r.title()).append("\n");
            if (r.snippet() != null && !r.snippet().isBlank()) {
                sb.append("   - Özet: ").append(r.snippet()).append("\n");
            }
            sb.append("   - URL: ").append(r.url()).append("\n");
        }
        sb.append("Yalnızca güvenilir bulduklarını kullan ve recommended_resources içinde bu URL'leri listele.\n");
        return sb.toString();
    }
}