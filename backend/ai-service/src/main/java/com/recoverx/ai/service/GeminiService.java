package com.recoverx.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recoverx.ai.dto.AIAnalysisRequest;
import com.recoverx.ai.exception.AIAnalysisException;
import com.recoverx.ai.exception.AIProviderException;
import com.recoverx.ai.dto.PaymentAnalysisResponse;
import com.recoverx.ai.dto.PaymentAnalysisType;
import com.recoverx.ai.prompt.PaymentAnalysisPrompt;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class GeminiService {
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final ProposedDateNormalizer dateNormalizer;

    public GeminiService(ChatModel chatModel, ObjectMapper objectMapper,
                         ProposedDateNormalizer dateNormalizer) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
        this.dateNormalizer = dateNormalizer;
    }

    public PaymentAnalysisResponse analyzePaymentMessage(AIAnalysisRequest request) {
        try {
            String prompt = PaymentAnalysisPrompt.buildPrompt(request.getMessage());
            String raw = chatModel.call(new Prompt(prompt)).getResult().getOutput().getText();
            if (raw == null || raw.isBlank()) {
                throw new AIAnalysisException("Gemini returned an empty response");
            }

            PaymentAnalysisResponse response = objectMapper.readValue(
                    extractJson(raw), PaymentAnalysisResponse.class);

            if (response.getAnalysisType() == null) {
                throw new AIAnalysisException("Gemini response did not contain a valid analysisType");
            }

            // Enforce application-side invariants rather than trusting model-generated flags.
            response.setPaymentIntent(
                    response.getAnalysisType() == PaymentAnalysisType.PAYMENT_PROMISE);

            if (response.getAnalysisType() != PaymentAnalysisType.PAYMENT_PROMISE) {
                response.setAmount(null);
                response.setProposedDateText(null);
                response.setProposedDate(null);
            } else {
                BigDecimal amount = response.getAmount();
                if (amount != null && amount.signum() <= 0) response.setAmount(null);

                response.setProposedDate(dateNormalizer.normalize(
                        response.getProposedDateText(),
                        request.getMessageTimestamp(),
                        request.getTimezone()));
            }

            response.setBusinessId(request.getBusinessId());
            response.setInvoiceId(request.getInvoiceId());
            response.setConversationId(request.getConversationId());
            response.setSourceMessageId(request.getSourceMessageId());
            if (response.getSummary() == null || response.getSummary().isBlank()) {
                response.setSummary("No summary was returned by the AI model.");
            } else if (response.getSummary().length() > 1000) {
                response.setSummary(response.getSummary().substring(0, 1000));
            }
            return response;
        } catch (JsonProcessingException ex) {
            throw new AIAnalysisException("Gemini response could not be parsed as the expected JSON", ex);
        } catch (AIAnalysisException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AIProviderException("AI analysis could not be completed. Check Gemini configuration and availability.", ex);
        }
    }

    private String extractJson(String raw) {
        String text = raw.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```(?:json)?\\s*", "");
            text = text.replaceFirst("\\s*```$", "");
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new AIAnalysisException("Gemini response did not contain a JSON object");
        }
        return text.substring(start, end + 1);
    }
}
