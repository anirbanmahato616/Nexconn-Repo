
package com.recoverx.commitment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomerCommitmentResponse {

    private Long id;
    private Long businessId;
    private Long invoiceId;
    private String summary;
    private BigDecimal proposedAmount;
    private LocalDate proposedPaymentDate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    public CustomerCommitmentResponse(
            Long id,
            Long businessId,
            Long invoiceId,
            String summary,
            BigDecimal proposedAmount,
            LocalDate proposedPaymentDate,
            String status,
            LocalDateTime createdAt,
            LocalDateTime reviewedAt) {
        this.id = id;
        this.businessId = businessId;
        this.invoiceId = invoiceId;
        this.summary = summary;
        this.proposedAmount = proposedAmount;
        this.proposedPaymentDate = proposedPaymentDate;
        this.status = status;
        this.createdAt = createdAt;
        this.reviewedAt = reviewedAt;
    }

    public Long getId() { return id; }
    public Long getBusinessId() { return businessId; }
    public Long getInvoiceId() { return invoiceId; }
    public String getSummary() { return summary; }
    public BigDecimal getProposedAmount() { return proposedAmount; }
    public LocalDate getProposedPaymentDate() { return proposedPaymentDate; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
}
