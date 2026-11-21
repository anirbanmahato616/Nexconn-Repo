package com.recoverx.ai.dto;

import java.math.BigDecimal;

public class PaymentAnalysisResponse {
    private Long businessId;
    private Long invoiceId;
    private Long conversationId;
    private Long sourceMessageId;
    private PaymentAnalysisType analysisType;
    private boolean paymentIntent;
    private BigDecimal amount;
    private String proposedDateText;
    private String proposedDate;
    private String summary;

    public PaymentAnalysisResponse() {}
    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public Long getSourceMessageId() { return sourceMessageId; }
    public void setSourceMessageId(Long sourceMessageId) { this.sourceMessageId = sourceMessageId; }
    public PaymentAnalysisType getAnalysisType() { return analysisType; }
    public void setAnalysisType(PaymentAnalysisType analysisType) { this.analysisType = analysisType; }
    public boolean isPaymentIntent() { return paymentIntent; }
    public void setPaymentIntent(boolean paymentIntent) { this.paymentIntent = paymentIntent; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getProposedDateText() { return proposedDateText; }
    public void setProposedDateText(String proposedDateText) { this.proposedDateText = proposedDateText; }
    public String getProposedDate() { return proposedDate; }
    public void setProposedDate(String proposedDate) { this.proposedDate = proposedDate; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
