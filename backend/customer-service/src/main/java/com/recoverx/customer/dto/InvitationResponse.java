package com.recoverx.customer.dto;

import java.time.LocalDateTime;

public class InvitationResponse {
    private String message;
    private Long businessId;
    private Long customerId;
    private String invitedEmail;
    private LocalDateTime expiresAt;

    public InvitationResponse(String message, Long businessId, Long customerId,
            String invitedEmail, LocalDateTime expiresAt) {
        this.message = message;
        this.businessId = businessId;
        this.customerId = customerId;
        this.invitedEmail = invitedEmail;
        this.expiresAt = expiresAt;
    }

    public String getMessage() { return message; }
    public Long getBusinessId() { return businessId; }
    public Long getCustomerId() { return customerId; }
    public String getInvitedEmail() { return invitedEmail; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}
