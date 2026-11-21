
package com.recoverx.conversation.dto;

import java.time.LocalDateTime;

public class CustomerConversationResponse {

    private Long id;
    private Long businessId;
    private Long invoiceId;
    private Long ownerUserId;
    private String nexconnChannelId;
    private LocalDateTime createdAt;
    private String ownerNexconnUserId;
    private String businessName;
    private String businessEmail;
    
    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessEmail() {
        return businessEmail;
    }

    public void setBusinessEmail(String businessEmail) {
        this.businessEmail = businessEmail;
    }

    public String getOwnerNexconnUserId() {
        return ownerNexconnUserId;
    }

    public void setOwnerNexconnUserId(String ownerNexconnUserId) {
        this.ownerNexconnUserId = ownerNexconnUserId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }

    public String getNexconnChannelId() { return nexconnChannelId; }
    public void setNexconnChannelId(String nexconnChannelId) {
        this.nexconnChannelId = nexconnChannelId;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
