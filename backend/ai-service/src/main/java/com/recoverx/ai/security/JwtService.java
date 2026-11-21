package com.recoverx.ai.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey signingKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public AuthenticatedUser validateAndExtract(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String email = claims.getSubject();
        Object rawUserId = claims.get("userId");
        if (email == null || email.isBlank() || !(rawUserId instanceof Number)) {
            throw new IllegalArgumentException("JWT is missing required identity claims");
        }

        Long userId = ((Number) rawUserId).longValue();
        if (userId <= 0) {
            throw new IllegalArgumentException("JWT contains an invalid userId");
        }
        return new AuthenticatedUser(userId, email);
    }
}
