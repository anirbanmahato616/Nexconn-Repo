package com.recoverx.commitment.security;

public class AuthenticatedUser {
    private final Long userId;
    private final String email;
    private final String token;

    public AuthenticatedUser(Long userId, String email, String token) {
        this.userId = userId;
        this.email = email;
        this.token = token;
    }

    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getToken() { return token; }
}
