package com.recoverx.commitment.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CommitmentRequest {
    private Long invoiceId;
    @NotBlank @Size(max = 190)
    private String sourceMessageId;
    @Positive
    private Long conversationId;
    @NotBlank @Size(max = 1000)
    private String summary;
    @Size(max = 40)
    private String aiClassification;
    @DecimalMin("0.01") @Digits(integer = 10, fraction = 2)
    private BigDecimal proposedAmount;
    private LocalDate proposedPaymentDate;

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public String getSourceMessageId() { return sourceMessageId; }
    public void setSourceMessageId(String sourceMessageId) { this.sourceMessageId = sourceMessageId; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getAiClassification() { return aiClassification; }
    public void setAiClassification(String aiClassification) { this.aiClassification = aiClassification; }
    public BigDecimal getProposedAmount() { return proposedAmount; }
    public void setProposedAmount(BigDecimal proposedAmount) { this.proposedAmount = proposedAmount; }
    public LocalDate getProposedPaymentDate() { return proposedPaymentDate; }
    public void setProposedPaymentDate(LocalDate proposedPaymentDate) { this.proposedPaymentDate = proposedPaymentDate; }
}
