package com.recoverx.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AcceptInvitationRequest {
    @NotBlank(message = "Invitation token is required")
    @Size(max = 200, message = "Invitation token is invalid")
    private String token;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
