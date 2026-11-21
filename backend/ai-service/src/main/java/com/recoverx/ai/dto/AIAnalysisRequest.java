package com.recoverx.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public class AIAnalysisRequest {
    @NotBlank(message = "Message cannot be empty")
    @Size(max = 10000, message = "Message cannot exceed 10000 characters")
    private String message;

    @NotNull(message = "Message timestamp is required")
    private Instant messageTimestamp;

    @NotBlank(message = "Timezone is required")
    @Size(max = 100, message = "Timezone cannot exceed 100 characters")
    private String timezone;

    // Required tenant context used to authorize this AI analysis request.
    @NotNull(message = "Business ID is required")
    @Positive(message = "Business ID must be positive")
    private Long businessId;
    @Positive(message = "Invoice ID must be positive")
    private Long invoiceId;
    @Positive(message = "Conversation ID must be positive")
    private Long conversationId;
    @Positive(message = "Source message ID must be positive")
    private Long sourceMessageId;

    public AIAnalysisRequest() {}
    public AIAnalysisRequest(String message, Instant messageTimestamp, String timezone) {
        this.message = message; this.messageTimestamp = messageTimestamp; this.timezone = timezone;
    }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Instant getMessageTimestamp() { return messageTimestamp; }
    public void setMessageTimestamp(Instant messageTimestamp) { this.messageTimestamp = messageTimestamp; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public Long getSourceMessageId() { return sourceMessageId; }
    public void setSourceMessageId(Long sourceMessageId) { this.sourceMessageId = sourceMessageId; }
}
