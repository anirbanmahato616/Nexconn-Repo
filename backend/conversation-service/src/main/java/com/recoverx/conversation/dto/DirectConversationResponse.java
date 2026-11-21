package com.recoverx.conversation.dto;

import java.time.LocalDateTime;

public class DirectConversationResponse {
    private Long id;
    private Long businessId;
    private Long customerId;
    private Long ownerUserId;
    private Long customerUserId;
    private Long invoiceId;
    private String channelId;
    private String otherUserNexconnId;
    private String currentUserNexconnId;
    private LocalDateTime createdAt;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
    public Long getCustomerUserId() { return customerUserId; }
    public void setCustomerUserId(Long customerUserId) { this.customerUserId = customerUserId; }
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }
    public String getOtherUserNexconnId() { return otherUserNexconnId; }
    public void setOtherUserNexconnId(String otherUserNexconnId) { this.otherUserNexconnId = otherUserNexconnId; }
    public String getCurrentUserNexconnId() { return currentUserNexconnId; }
    public void setCurrentUserNexconnId(String currentUserNexconnId) { this.currentUserNexconnId = currentUserNexconnId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
