package com.recoverx.payment.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
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
        try {
            this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("jwt.secret must be a valid Base64-encoded signing key", ex);
        }
    }

    public AuthenticatedUser parse(String token) {
        Claims claims = Jwts.parser().verifyWith(signingKey).build()
                .parseSignedClaims(token).getPayload();
        Number userIdClaim = claims.get("userId", Number.class);
        String email = claims.getSubject();
        if (userIdClaim == null || userIdClaim.longValue() <= 0 || email == null || email.isBlank()) {
            throw new JwtException("Required JWT claims are missing");
        }
        return new AuthenticatedUser(userIdClaim.longValue(), email);
    }
}
