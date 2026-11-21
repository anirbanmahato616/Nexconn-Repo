
package com.recoverx.commitment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CommitmentInstallmentRequest {

    @NotNull(message = "Commitment ID is required")
    @Positive(message = "Commitment ID must be positive")
    private Long commitmentId;

    @NotNull(message = "Installment amount is required")
    @DecimalMin(value = "0.01", message = "Installment amount must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Amount can have up to 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "Proposed payment date is required")
    private LocalDate proposedDate;

    public CommitmentInstallmentRequest() {
    }

    public Long getCommitmentId() {
        return commitmentId;
    }

    public void setCommitmentId(Long commitmentId) {
        this.commitmentId = commitmentId;
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
