
package com.recoverx.auth.dto;

public class AuthResponse {

    private String message;
    private Long userId;
    private String fullName;
    private String email;
    private String accessToken;
    private String tokenType;

    public AuthResponse() {
    }

    public AuthResponse(String message, Long userId,
                        String fullName, String email,
                        String accessToken, String tokenType) {
        this.message = message;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.accessToken = accessToken;
        this.tokenType = tokenType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
