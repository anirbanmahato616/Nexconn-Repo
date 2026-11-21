package com.recoverx.commitment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Structured result returned by AI Service. AI output remains a proposal for human review. */
public class AICommitmentRequest {
    private Long invoiceId;

    @NotBlank(message = "Analysis type is required")
    private String analysisType;

    private boolean paymentIntent;

    @Positive(message = "Conversation ID must be positive")
    private Long conversationId;

    @Size(max = 190, message = "Source message ID cannot exceed 190 characters")
    private String sourceMessageId;

    @NotBlank(message = "AI summary is required")
    @Size(max = 1000, message = "Summary cannot exceed 1000 characters")
    private String summary;

    @DecimalMin(value = "0.01", message = "Proposed amount must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Amount can have up to 2 decimal places")
    private BigDecimal amount;

    private LocalDate proposedDate;

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public String getAnalysisType() { return analysisType; }
    public void setAnalysisType(String analysisType) { this.analysisType = analysisType; }
    public boolean isPaymentIntent() { return paymentIntent; }
    public void setPaymentIntent(boolean paymentIntent) { this.paymentIntent = paymentIntent; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public String getSourceMessageId() { return sourceMessageId; }
    public void setSourceMessageId(String sourceMessageId) { this.sourceMessageId = sourceMessageId; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getProposedDate() { return proposedDate; }
    public void setProposedDate(LocalDate proposedDate) { this.proposedDate = proposedDate; }
}
