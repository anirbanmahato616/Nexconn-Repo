package com.recoverx.conversation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AICommitmentRequest {

    @JsonIgnore
    private Long invoiceId;

    private String analysisType;
    private Boolean paymentIntent;
    private Long conversationId;
    private String sourceMessageId;
    private String summary;
    private BigDecimal amount;
    private LocalDate proposedDate;
	public Long getInvoiceId() {
		return invoiceId;
	}
	public void setInvoiceId(Long invoiceId) {
		this.invoiceId = invoiceId;
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
	public Long getConversationId() {
		return conversationId;
	}
	public void setConversationId(Long conversationId) {
		this.conversationId = conversationId;
	}
	public String getSourceMessageId() {
		return sourceMessageId;
	}
	public void setSourceMessageId(String sourceMessageId) {
		this.sourceMessageId = sourceMessageId;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
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

    
    
    
}