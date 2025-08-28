package com.example.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationSeconds;
    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration:3600}") long expirationSeconds
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("jwt.secret boş olamaz");
        }

        SecretKey tmpKey;
        if (secret.startsWith("base64:")) {
            byte[] keyBytes = Decoders.BASE64.decode(secret.substring("base64:".length()));
            tmpKey = Keys.hmacShaKeyFor(keyBytes);
        } else {
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < 32) { // 32 byte = 256 bit
                throw new IllegalArgumentException("jwt.secret en az 256 bit (32 byte) olmalı.");
            }
            tmpKey = Keys.hmacShaKeyFor(keyBytes);
        }

        this.key = tmpKey;
        this.expirationSeconds = expirationSeconds;
    }


    public String generateToken(String subject) {
        return generateToken(subject, null);
    }


    public String generateToken(String subject, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + (expirationSeconds * 1000));

        return Jwts.builder()
                .setClaims(extraClaims == null ? Map.of() : extraClaims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date exp = claims.getExpiration();
            return exp == null || exp.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }



    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        Date exp = extractExpiration(token);
        return exp != null && exp.before(new Date());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}