package com.recoverx.payment.dto;
import jakarta.validation.constraints.Size;

public class PaymentReviewRequest {
    @Size(max = 500, message = "Rejection reason cannot exceed 500 characters")
    private String reason;
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
