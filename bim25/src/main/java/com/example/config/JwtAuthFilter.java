package com.example.config;

import com.example.auth.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Public yollar
        if (path.startsWith("/api/auth")
                || path.startsWith("/api/health")
                || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            System.out.println("[JWT] MISSING/AUTH_HEADER_INVALID path=" + path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            return;
        }

        String token = authHeader.substring(7);
        String tokenPreview = token.length() > 12 ? token.substring(0, 12) + "..." : token;

        try {
            System.out.println("[JWT] Received token preview=" + tokenPreview + " path=" + path);

            // 1) subject
            String username = jwtService.extractUsername(token);
            if (username == null || username.isBlank()) {
                System.out.println("[JWT] Username is NULL/BLANK");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 2) token geçerli mi?
            boolean valid = jwtService.validateToken(token);
            System.out.println("[JWT] path=" + path + " user=" + username + " valid=" + valid);

            if (!valid) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // 3) SecurityContext
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            System.out.println("[JWT] EXCEPTION: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            // Beklenmeyen hata => 500
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            // İstersen üst kata fırlatıp Spring'in default 500 cevabını da kullanabilirsin:
            // throw e;
        }
    }
}