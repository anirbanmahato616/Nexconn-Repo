package com.recoverx.conversation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ConversationRequest {
    @NotNull(message = "Invoice ID is required")
    @Positive(message = "Invoice ID must be positive")
    private Long invoiceId;

    @NotBlank(message = "Nexconn channel ID is required")
    @Size(max = 190, message = "Nexconn channel ID cannot exceed 190 characters")
    private String nexconnChannelId;

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public String getNexconnChannelId() { return nexconnChannelId; }
    public void setNexconnChannelId(String nexconnChannelId) { this.nexconnChannelId = nexconnChannelId; }
}
