package com.recoverx.conversation.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "conversations",
    uniqueConstraints = @UniqueConstraint(name = "uk_conversation_business_owner_customer", columnNames = {"business_id", "owner_user_id", "customer_user_id"}),
    indexes = {
        @Index(name = "idx_conversation_business_invoice", columnList = "business_id,invoice_id")
    })
public class Conversation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "customer_user_id")
    private Long customerUserId;

    @Column(name = "nexconn_channel_id", nullable = false, length = 190)
    private String nexconnChannelId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Conversation() {}
    @PrePersist
    public void onCreate() { this.createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getCustomerUserId() { return customerUserId; }
    public void setCustomerUserId(Long customerUserId) { this.customerUserId = customerUserId; }
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public String getNexconnChannelId() { return nexconnChannelId; }
    public void setNexconnChannelId(String nexconnChannelId) { this.nexconnChannelId = nexconnChannelId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
