package com.recoverx.conversation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.recoverx.conversation.client.AIClient;
import com.recoverx.conversation.client.CommitmentClient;
import com.recoverx.conversation.dto.AIAnalysisRequest;
import com.recoverx.conversation.dto.AIAnalysisResponse;
import com.recoverx.conversation.dto.AICommitmentRequest;

@Service
public class PaymentPromiseProcessingService {

    private static final Logger log = LoggerFactory.getLogger(PaymentPromiseProcessingService.class);

    private final AIClient aiClient;
    private final CommitmentClient commitmentClient;

    @Value("${services.internal.api-key}")
    private String internalApiKey;

    public PaymentPromiseProcessingService(AIClient aiClient, CommitmentClient commitmentClient) {
        this.aiClient = aiClient;
        this.commitmentClient = commitmentClient;
    }

    public void processPaymentPromise(Long businessId, AIAnalysisRequest aiRequest) {

        // 1. Ask the AI service to analyze the message
        AIAnalysisResponse analysis = aiClient.analyzeMessage(internalApiKey, aiRequest);

        // 2. Only a positive PAYMENT_PROMISE continues
        if (analysis == null
                || !Boolean.TRUE.equals(analysis.getPaymentIntent())
                || !"PAYMENT_PROMISE".equalsIgnoreCase(analysis.getAnalysisType())) {
            return;
        }

        // 3. Map to the commitment request, falling back to the original request's IDs
        Long invoiceId = analysis.getInvoiceId() != null ? analysis.getInvoiceId() : aiRequest.getInvoiceId();
        Long conversationId = analysis.getConversationId() != null
                ? analysis.getConversationId() : aiRequest.getConversationId();
        Long sourceMessageId = analysis.getSourceMessageId() != null
                ? analysis.getSourceMessageId() : aiRequest.getSourceMessageId();

        AICommitmentRequest commitment = new AICommitmentRequest();
        commitment.setInvoiceId(invoiceId);
        commitment.setConversationId(conversationId);
        commitment.setSourceMessageId(sourceMessageId == null ? null : String.valueOf(sourceMessageId));
        commitment.setAnalysisType(analysis.getAnalysisType());
        commitment.setPaymentIntent(true);
        commitment.setAmount(analysis.getAmount());
        commitment.setProposedDate(analysis.getProposedDate());
        commitment.setSummary(analysis.getSummary() != null ? analysis.getSummary() : aiRequest.getMessage());

        // 4. Create the commitment (it stays PENDING_REVIEW on the Commitment Service side)
        commitmentClient.createFromAIAnalysis(businessId, internalApiKey, commitment);
        log.info("Commitment requested for invoice {} from message {}", invoiceId, sourceMessageId);
    }
}