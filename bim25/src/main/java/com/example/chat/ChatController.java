package com.example.chat;

import com.example.chat.dto.ChatDtos.ChatRequest;
import com.example.chat.dto.ChatDtos.ChatResponse;
import com.example.domain.user.User;
import com.example.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    public ChatController(ChatService chatService, UserRepository userRepository) {
        this.chatService = chatService;
        this.userRepository = userRepository;
    }

    //  Mesaj gönder
    @PostMapping("/ask")
    public ResponseEntity<?> ask(@RequestBody ChatRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }

        ChatResponse res = chatService.ask(user, req);
        return ResponseEntity.ok(res);
    }

    // Eski history
    @GetMapping("/history")
    public ResponseEntity<?> history(@RequestParam(defaultValue = "50") int limit) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(401).body("User not found");

        return ResponseEntity.ok(chatService.history(user.getId(), limit));
    }

    // Sohbet oluştur
    @PostMapping("/conversations")
    public ResponseEntity<?> createConversation(@RequestBody(required = false) java.util.Map<String, String> body) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal()))
            return ResponseEntity.status(401).body("Unauthorized");
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(401).body("User not found");

        String title = body != null ? body.getOrDefault("title", "Yeni sohbet") : "Yeni sohbet";
        Long id = chatService.createConversation(user.getId(), title);
        return ResponseEntity.ok(java.util.Map.of("id", id, "title", title));
    }

    // Sohbet listesi
    @GetMapping("/conversations")
    public ResponseEntity<?> conversations() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal()))
            return ResponseEntity.status(401).body("Unauthorized");
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(401).body("User not found");

        return ResponseEntity.ok(chatService.listConversations(user.getId()));
    }

    // Bir sohbetin mesajları
    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<?> conversationMessages(@PathVariable Long id,
                                                  @RequestParam(defaultValue = "0") int limit) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal()))
            return ResponseEntity.status(401).body("Unauthorized");
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(401).body("User not found");

        return ResponseEntity.ok(chatService.conversationMessages(user.getId(), id, limit));
    }
}