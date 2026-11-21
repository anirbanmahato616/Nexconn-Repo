package com.recoverx.commitment.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
public class CommitmentResponse {
    private Long id, businessId, invoiceId, conversationId, reviewedBy;
    private String sourceMessageId, summary, status, aiClassification, reviewNote;
    private BigDecimal proposedAmount;
    private LocalDate proposedPaymentDate;
    private LocalDateTime reviewedAt, createdAt;
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Long getBusinessId(){return businessId;} public void setBusinessId(Long v){businessId=v;}
    public Long getInvoiceId(){return invoiceId;} public void setInvoiceId(Long v){invoiceId=v;}
    public Long getConversationId(){return conversationId;} public void setConversationId(Long v){conversationId=v;}
    public Long getReviewedBy(){return reviewedBy;} public void setReviewedBy(Long v){reviewedBy=v;}
    public String getSourceMessageId(){return sourceMessageId;} public void setSourceMessageId(String v){sourceMessageId=v;}
    public String getSummary(){return summary;} public void setSummary(String v){summary=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getAiClassification(){return aiClassification;} public void setAiClassification(String v){aiClassification=v;}
    public String getReviewNote(){return reviewNote;} public void setReviewNote(String v){reviewNote=v;}
    public BigDecimal getProposedAmount(){return proposedAmount;} public void setProposedAmount(BigDecimal v){proposedAmount=v;}
    public LocalDate getProposedPaymentDate(){return proposedPaymentDate;} public void setProposedPaymentDate(LocalDate v){proposedPaymentDate=v;}
    public LocalDateTime getReviewedAt(){return reviewedAt;} public void setReviewedAt(LocalDateTime v){reviewedAt=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
