package com.recoverx.conversation.client;

import com.recoverx.conversation.dto.AICommitmentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Sends eligible AI payment-promise analyses to Commitment Service.
 * Commitment Service creates PENDING_REVIEW only; owner/admin approval remains separate.
 */
@Component
public class CommitmentServiceClient {

    private final RestClient restClient;
    private final String internalApiKey;

    public CommitmentServiceClient(
            RestClient.Builder builder,
            @Value("${services.commitment.base-url:http://localhost:8084}") String baseUrl,
            @Value("${recoverx.internal-api-key:}") String internalApiKey) {
        this.restClient = builder.clone().baseUrl(baseUrl).build();
        this.internalApiKey = internalApiKey;
    }

    public void createFromAIAnalysis(Long businessId, AICommitmentRequest request) {
        if (internalApiKey == null || internalApiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Internal API key is not configured for Commitment Service");
        }

        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("invoiceId", 5L); // TEMP hardcode
        body.put("analysisType", request.getAnalysisType());
        body.put("paymentIntent", request.getPaymentIntent());
        body.put("conversationId", request.getConversationId());
        body.put("sourceMessageId", request.getSourceMessageId());
        body.put("summary", request.getSummary());
        body.put("amount", request.getAmount());
        body.put("proposedDate", request.getProposedDate() == null ? null : request.getProposedDate().toString());
        System.out.println("SENDING TO COMMITMENT SERVICE: " + body);

        try {
            restClient.post()
                    .uri("/api/internal/businesses/{businessId}/commitments/from-ai-analysis", businessId)
                    .header("X-Internal-Api-Key", internalApiKey)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (org.springframework.web.client.RestClientResponseException ex) {
            System.out.println("COMMITMENT SERVICE REPLIED " + ex.getStatusCode().value()
                    + ": " + ex.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Conversation Service could not create the pending commitment", ex);
        } catch (RestClientException ex) {
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Conversation Service could not create the pending commitment", ex);
        }
    }
}
