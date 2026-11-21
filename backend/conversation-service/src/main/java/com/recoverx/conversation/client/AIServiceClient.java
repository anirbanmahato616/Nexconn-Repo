package com.recoverx.conversation.client;

import com.recoverx.conversation.dto.InternalAIAnalysisRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;

@Component
public class AIServiceClient {
    private final RestClient restClient;
    private final String internalApiKey;

    public AIServiceClient(RestClient.Builder builder,
            @Value("${services.ai.base-url:http://localhost:8086}") String baseUrl,
            @Value("${recoverx.internal-api-key:}") String internalApiKey) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.internalApiKey = internalApiKey;
    }

    public JsonNode analyze(InternalAIAnalysisRequest request) {
        if (internalApiKey == null || internalApiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Internal AI service key is not configured");
        }
        try {
            JsonNode response = restClient.post()
                    .uri("/api/ai/analyze/internal")
                    .header("X-Internal-Api-Key", internalApiKey)
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "AI Service returned an empty response");
            }
            return response;
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Conversation Service could not complete AI message analysis", ex);
        }
    }
}
