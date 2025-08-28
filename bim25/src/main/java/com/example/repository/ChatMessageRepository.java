package com.example.repository;

import com.example.domain.chat.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {


    List<ChatMessage> findByUserIdOrderByCreatedAtAsc(Long userId);
    List<ChatMessage> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);


    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
    List<ChatMessage> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);
    long countByConversationId(Long conversationId);
}