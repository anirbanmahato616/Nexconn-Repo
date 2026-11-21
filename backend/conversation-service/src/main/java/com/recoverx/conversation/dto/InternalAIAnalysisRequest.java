package com.recoverx.conversation.dto;

import java.time.Instant;

public class InternalAIAnalysisRequest {
    private Long businessId;
    private Long invoiceId;
    private Long conversationId;
    private Long sourceMessageId;
    private String message;
    private Instant messageTimestamp;
    private String timezone;

    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public Long getSourceMessageId() { return sourceMessageId; }
    public void setSourceMessageId(Long sourceMessageId) { this.sourceMessageId = sourceMessageId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Instant getMessageTimestamp() { return messageTimestamp; }
    public void setMessageTimestamp(Instant messageTimestamp) { this.messageTimestamp = messageTimestamp; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
}
