
package com.recoverx.business.dto;

import java.time.LocalDateTime;

import com.recoverx.business.entity.BusinessRole;

public class MembershipResponse {

    private Long id;
    private Long businessId;
    private Long userId;
    private BusinessRole role;
    private Boolean active;
    private LocalDateTime createdAt;

    public MembershipResponse() {
    }

    public MembershipResponse(
            Long id,
            Long businessId,
            Long userId,
            BusinessRole role,
            Boolean active,
            LocalDateTime createdAt) {

        this.id = id;
        this.businessId = businessId;
        this.userId = userId;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public Long getUserId() {
        return userId;
    }

    public BusinessRole getRole() {
        return role;
    }

    public Boolean getActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
