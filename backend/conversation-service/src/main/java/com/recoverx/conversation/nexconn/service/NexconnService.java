package com.recoverx.conversation.nexconn.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.databind.JsonNode;
import com.recoverx.conversation.nexconn.config.NexconnProperties;
import com.recoverx.conversation.nexconn.dto.NexconnTokenResponse;

@Service
public class NexconnService {
    private final RestClient restClient;
    private final NexconnProperties properties;
    private final NexconnSignature signature;

    public NexconnService(RestClient nexconnRestClient, NexconnProperties properties,
            NexconnSignature signature) {
        this.restClient = nexconnRestClient;
        this.properties = properties;
        this.signature = signature;
    }

    public NexconnTokenResponse issueToken(Long recoverxUserId, String email) {
        if (properties.getAppKey() == null || properties.getAppKey().isBlank()
                || properties.getAppSecret() == null || properties.getAppSecret().isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Nexconn is not configured. Set NEXCONN_APP_KEY and NEXCONN_APP_SECRET.");
        }
        String nexconnUserId = toNexconnUserId(recoverxUserId);
        String nonce = signature.nonce();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String signed = signature.sign(properties.getAppSecret(), nonce, timestamp);
        String displayName = email == null || email.isBlank() ? "RecoverX User" : email;
        if (displayName.length() > 64) displayName = displayName.substring(0, 64);
        try {
            JsonNode response = restClient.post()
                    .uri("/v4/auth/access-token/issue")
                    .header("App-Key", properties.getAppKey())
                    .header("Nonce", nonce)
                    .header("Timestamp", timestamp)
                    .header("Signature", signed)
                    .body(Map.of("userId", nexconnUserId, "name", displayName))
                    .retrieve().body(JsonNode.class);
            if (response == null || response.path("code").asInt(-1) != 0
                    || response.path("result").path("accessToken").asText().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Nexconn did not return a valid access token");
            }
            return new NexconnTokenResponse(nexconnUserId,
                    response.path("result").path("accessToken").asText());
        } catch (RestClientException ex) {
            ex.printStackTrace();

            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Could not obtain a Nexconn access token",
                ex
            );
        }
    }

    public String toNexconnUserId(Long userId) {
        if (userId == null || userId <= 0) throw new IllegalArgumentException("RecoverX user ID must be positive");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((properties.getAppKey() + ":recoverx:" + userId)
                            .getBytes(StandardCharsets.UTF_8));
            return "rx_" + HexFormat.of().formatHex(digest, 0, 16);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not generate Nexconn user ID", ex);
        }
    }
}
