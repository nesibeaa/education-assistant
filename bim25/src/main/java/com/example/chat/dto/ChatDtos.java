package com.example.chat.dto;

import java.time.Instant;
import java.util.List;

public class ChatDtos {

    // İstek
    public static class ChatRequest {
        private String message;

        //sohbet mantığı
        private Long conversationId; // null ise backend yeni sohbet açar
        private String title;        // yeni sohbete opsiyonel başlık

        public ChatRequest() {}
        public ChatRequest(String message) { this.message = message; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Long getConversationId() { return conversationId; }
        public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }

    // Kaynak linki
    public static class ResourceLink {
        private String title;
        private String url;

        public ResourceLink() {}
        public ResourceLink(String title, String url) {
            this.title = title;
            this.url = url;
        }
        public String getTitle() { return title; }
        public String getUrl() { return url; }
        public void setTitle(String title) { this.title = title; }
        public void setUrl(String url) { this.url = url; }
    }

    // Model cevabı
    public static class ChatResponse {
        private Long id;
        private Long userId;
        private String request;
        private String reply;
        private String status;
        private String topic;
        private String difficulty;
        private String nextStep;
        private Double confidence;
        private Instant createdAt;
        private String source;
        private List<ResourceLink> resources;

        public ChatResponse() {}

        public ChatResponse(Long id, Long userId, String request, String reply,
                            String status, String topic, String difficulty,
                            String nextStep, Double confidence, Instant createdAt,
                            String source, List<ResourceLink> resources) {
            this.id = id; this.userId = userId; this.request = request; this.reply = reply;
            this.status = status; this.topic = topic; this.difficulty = difficulty;
            this.nextStep = nextStep; this.confidence = confidence; this.createdAt = createdAt;
            this.source = source; this.resources = resources;
        }

        public Long getId() { return id; }
        public Long getUserId() { return userId; }
        public String getRequest() { return request; }
        public String getReply() { return reply; }
        public String getStatus() { return status; }
        public String getTopic() { return topic; }
        public String getDifficulty() { return difficulty; }
        public String getNextStep() { return nextStep; }
        public Double getConfidence() { return confidence; }
        public Instant getCreatedAt() { return createdAt; }
        public String getSource() { return source; }
        public List<ResourceLink> getResources() { return resources; }

        public void setId(Long id) { this.id = id; }
        public void setUserId(Long userId) { this.userId = userId; }
        public void setRequest(String request) { this.request = request; }
        public void setReply(String reply) { this.reply = reply; }
        public void setStatus(String status) { this.status = status; }
        public void setTopic(String topic) { this.topic = topic; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public void setNextStep(String nextStep) { this.nextStep = nextStep; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }
        public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
        public void setSource(String source) { this.source = source; }
        public void setResources(List<ResourceLink> resources) { this.resources = resources; }
    }

    // Geçmiş listesi (sohbet) özet DTO
    public static class ConversationSummary {
        private Long id;
        private String title;
        private String lastSnippet;
        private Integer messageCount;
        private Instant updatedAt;

        public ConversationSummary() {}
        public ConversationSummary(Long id, String title, String lastSnippet,
                                   Integer messageCount, Instant updatedAt) {
            this.id = id; this.title = title; this.lastSnippet = lastSnippet;
            this.messageCount = messageCount; this.updatedAt = updatedAt;
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getLastSnippet() { return lastSnippet; }
        public Integer getMessageCount() { return messageCount; }
        public Instant getUpdatedAt() { return updatedAt; }

        public void setId(Long id) { this.id = id; }
        public void setTitle(String title) { this.title = title; }
        public void setLastSnippet(String lastSnippet) { this.lastSnippet = lastSnippet; }
        public void setMessageCount(Integer messageCount) { this.messageCount = messageCount; }
        public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    }
}