package com.recoverx.invoice.security;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private final SecretKey secretKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public AuthenticatedUser parseUser(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String subject = claims.getSubject();
        String email = claims.get("email", String.class);
        Object userIdValue = claims.get("userId");

        if (subject == null || subject.isBlank()
                || email == null || !subject.equals(email)
                || userIdValue == null || claims.getExpiration() == null) {
            throw new IllegalArgumentException("JWT is missing required identity claims");
        }

        Long userId;
        if (userIdValue instanceof Number number) {
            userId = number.longValue();
        } else {
            userId = Long.valueOf(userIdValue.toString());
        }

        if (userId <= 0) {
            throw new IllegalArgumentException("JWT contains an invalid userId");
        }

        return new AuthenticatedUser(userId, email);
    }
}
