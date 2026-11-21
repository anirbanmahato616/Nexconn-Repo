package com.recoverx.conversation.nexconn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nexconn")
public class NexconnProperties {
    private String appKey;
    private String appSecret;
    private String baseUrl = "https://api.sg-light-api.com";
    private long webhookClockSkewSeconds = 300;

    public String getAppKey() { return appKey; }
    public void setAppKey(String appKey) { this.appKey = appKey; }
    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public long getWebhookClockSkewSeconds() { return webhookClockSkewSeconds; }
    public void setWebhookClockSkewSeconds(long webhookClockSkewSeconds) { this.webhookClockSkewSeconds = webhookClockSkewSeconds; }
}
