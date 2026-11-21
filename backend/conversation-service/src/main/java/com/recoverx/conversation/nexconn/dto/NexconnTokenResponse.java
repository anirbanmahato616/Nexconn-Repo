package com.recoverx.conversation.nexconn.dto;

public class NexconnTokenResponse {
    private String userId;
    private String accessToken;
    public NexconnTokenResponse() {}
    public NexconnTokenResponse(String userId, String accessToken) { this.userId = userId; this.accessToken = accessToken; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
}
