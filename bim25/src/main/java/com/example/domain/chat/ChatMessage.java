package com.example.domain.chat;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable = false)
    private Long userId;

    @Column(name="request", columnDefinition = "text", nullable = false)
    private String request;

    @Column(name="reply", columnDefinition = "text", nullable = false)
    private String reply;

    @Column(name="status", length = 100)
    private String status;

    @Column(name="topic", length = 100)
    private String topic;

    @Column(name="difficulty", length = 100)
    private String difficulty;

    @Column(name="next_step", columnDefinition = "text")
    private String nextStep;

    @Column(name="confidence")
    private Double confidence;

    @Column(name="language", length = 10)
    private String language;

    @Column(name="resources_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode resourcesJson;                    // recommended_resources array/object

    @Column(name="created_at", nullable = false, updatable = false)
    private Instant createdAt;


    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;


    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public ChatMessage() {}

    /** Kısa kurucu */
    public ChatMessage(Long userId, String request, String reply, Instant createdAt) {
        this.userId = userId;
        this.request = request;
        this.reply = reply;
        this.createdAt = createdAt;
    }

    /** Tam kurucu –– ChatService bununla kaydediyor */
    public ChatMessage(Long userId,
                       String request,
                       String reply,
                       Instant createdAt,
                       String status,
                       String topic,
                       String nextStep,
                       Double confidence,
                       String language,
                       String difficulty,
                       JsonNode resourcesJson) {
        this.userId = userId;
        this.request = request;
        this.reply = reply;
        this.createdAt = createdAt;
        this.status = status;
        this.topic = topic;
        this.nextStep = nextStep;
        this.confidence = confidence;
        this.language = language;
        this.difficulty = difficulty;
        this.resourcesJson = resourcesJson;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = Instant.now();
    }

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getRequest() { return request; }
    public void setRequest(String request) { this.request = request; }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getNextStep() { return nextStep; }
    public void setNextStep(String nextStep) { this.nextStep = nextStep; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public JsonNode getResourcesJson() { return resourcesJson; }
    public void setResourcesJson(JsonNode resourcesJson) { this.resourcesJson = resourcesJson; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}