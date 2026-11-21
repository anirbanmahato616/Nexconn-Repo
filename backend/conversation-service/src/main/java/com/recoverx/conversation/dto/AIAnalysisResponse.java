
package com.recoverx.conversation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AIAnalysisResponse {

    private Long businessId;
    private Long conversationId;
    private Long invoiceId;
    private Long sourceMessageId;

    private String analysisType;
    private Boolean paymentIntent;

    private BigDecimal amount;
    private LocalDate proposedDate;
    private String proposedDateText;
    private String summary;

    public AIAnalysisResponse() {
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Long getSourceMessageId() {
        return sourceMessageId;
    }

    public void setSourceMessageId(Long sourceMessageId) {
        this.sourceMessageId = sourceMessageId;
    }

    public String getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }

    public Boolean getPaymentIntent() {
        return paymentIntent;
    }

    public void setPaymentIntent(Boolean paymentIntent) {
        this.paymentIntent = paymentIntent;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getProposedDate() {
        return proposedDate;
    }

    public void setProposedDate(LocalDate proposedDate) {
        this.proposedDate = proposedDate;
    }

    public String getProposedDateText() {
        return proposedDateText;
    }

    public void setProposedDateText(String proposedDateText) {
        this.proposedDateText = proposedDateText;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
