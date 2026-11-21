package com.recoverx.auth.security;

import java.util.Date;

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
    private final long expiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {

        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;
    }

    // Generate a signed JWT containing both the user's email and database ID.
    public String generateToken(Long userId, String email) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object userId = extractAllClaims(token).get("userId");

        if (userId == null) {
            throw new IllegalArgumentException("JWT does not contain userId");
        }

        if (userId instanceof Number number) {
            return number.longValue();
        }

        throw new IllegalArgumentException("JWT userId claim is invalid");
    }

    public boolean isTokenValid(String token, String email, Long userId) {
        try {
            Claims claims = extractAllClaims(token);

            String tokenEmail = claims.getSubject();
            Object tokenUserId = claims.get("userId");

            return tokenEmail != null
                    && tokenEmail.equals(email)
                    && tokenUserId instanceof Number
                    && ((Number) tokenUserId).longValue() == userId
                    && claims.getExpiration() != null
                    && claims.getExpiration().after(new Date());

        } catch (Exception ex) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
