package com.example.auth;

import com.example.auth.dto.AuthDtos.AuthResponse;
import com.example.auth.dto.AuthDtos.LoginRequest;
import com.example.auth.dto.AuthDtos.RegisterRequest;
import com.example.domain.user.User;
import com.example.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
        }

        User u = new User();
        u.setEmail(req.email());
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        userRepository.save(u);

        String token = jwtService.generateToken(u.getEmail());
        return ResponseEntity
                .created(URI.create("/api/users/" + u.getId()))
                .body(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest req) {
        return userRepository.findByEmail(req.email())
                .map(u -> {
                    if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
                        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
                    }
                    String token = jwtService.generateToken(u.getEmail());
                    return ResponseEntity.ok(new AuthResponse(token));
                })
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "Invalid credentials")));
    }


    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        return ResponseEntity.ok(Map.of("email", auth.getName()));
    }
}