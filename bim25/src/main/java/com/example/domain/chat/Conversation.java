package com.example.domain.chat;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "conversation", indexes = {
        @Index(name = "ix_conversation_user", columnList = "user_id"),
        @Index(name = "ix_conversation_updated", columnList = "updated_at")
})
public class Conversation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Conversation() {}

    public Conversation(Long userId, String title) {
        this.userId = userId;
        this.title = (title == null || title.isBlank()) ? "Yeni sohbet" : title.trim();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void touch() { this.updatedAt = Instant.now(); }

    // getters & setters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getTitle() { return title; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setTitle(String title) { this.title = title; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}