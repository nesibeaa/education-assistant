package com.example.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SerperClient {

    private final WebClient web;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${serper.api.key:}")
    private String apiKey;

    @Value("${search.topN:3}")
    private int topN;

    public SerperClient() {
        this.web = WebClient.builder()
                .baseUrl("https://google.serper.dev")
                .build();
    }

    public boolean isEnabled() {
        return apiKey != null && !apiKey.isBlank();
    }

    /** Basit link listesi: {title,url} */
    public List<Map<String, String>> searchLinks(String query) {
        List<Map<String, String>> out = new ArrayList<>();
        if (!isEnabled()) return out;
        try {
            String json = web.post()
                    .uri("/search")
                    .header("X-API-KEY", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("q", query, "num", topN))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = mapper.readTree(json);

            // organic sonuçlar
            JsonNode organic = root.path("organic");
            int added = 0;
            if (organic.isArray()) {
                for (JsonNode item : organic) {
                    String title = item.path("title").asText("");
                    String link  = item.path("link").asText("");
                    if (!title.isBlank() && !link.isBlank()) {
                        out.add(Map.of("title", title, "url", link));
                        if (++added >= topN) break;
                    }
                }
            }

            // hiç yoksa knowledgeGraph, answerBox vs. dene (opsiyonel)
            if (out.isEmpty()) {
                JsonNode kb = root.path("knowledgeGraph");
                if (kb.isObject()) {
                    String title = kb.path("title").asText("");
                    String link  = kb.path("website").asText("");
                    if (!title.isBlank() && !link.isBlank()) {
                        out.add(Map.of("title", title, "url", link));
                    }
                }
            }

            return out;
        } catch (Exception e) {
            System.out.println("[SERPER] error: " + e.getMessage());
            return out;
        }
    }
}