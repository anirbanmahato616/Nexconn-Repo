package com.recoverx.commitment.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "commitments", indexes = {
    @Index(name = "idx_commitment_business_invoice", columnList = "business_id,invoice_id"),
    @Index(name = "idx_commitment_status", columnList = "status")
})
public class Commitment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "invoice_id", nullable = true)
    private Long invoiceId;

    @Column(name = "conversation_id")
    private Long conversationId;

    @Column(name = "source_message_id", length = 190)
    private String sourceMessageId;

    @Column(length = 1000, nullable = false)
    private String summary;

    // AI output is stored as a suggestion; it never approves a commitment.
    @Column(name = "ai_classification", length = 40)
    private String aiClassification;

    @Column(name = "proposed_amount", precision = 12, scale = 2)
    private BigDecimal proposedAmount;

    @Column(name = "proposed_payment_date")
    private LocalDate proposedPaymentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CommitmentStatus status = CommitmentStatus.PENDING_REVIEW;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_note", length = 500)
    private String reviewNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public String getSourceMessageId() { return sourceMessageId; }
    public void setSourceMessageId(String sourceMessageId) { this.sourceMessageId = sourceMessageId; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getAiClassification() { return aiClassification; }
    public void setAiClassification(String aiClassification) { this.aiClassification = aiClassification; }
    public BigDecimal getProposedAmount() { return proposedAmount; }
    public void setProposedAmount(BigDecimal proposedAmount) { this.proposedAmount = proposedAmount; }
    public LocalDate getProposedPaymentDate() { return proposedPaymentDate; }
    public void setProposedPaymentDate(LocalDate proposedPaymentDate) { this.proposedPaymentDate = proposedPaymentDate; }
    public CommitmentStatus getStatus() { return status; }
    public void setStatus(CommitmentStatus status) { this.status = status; }
    public Long getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Long reviewedBy) { this.reviewedBy = reviewedBy; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
