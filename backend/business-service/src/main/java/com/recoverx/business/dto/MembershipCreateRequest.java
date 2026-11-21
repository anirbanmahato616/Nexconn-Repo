
package com.recoverx.business.dto;

import com.recoverx.business.entity.BusinessRole;

import jakarta.validation.constraints.NotNull;

public class MembershipCreateRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Business role is required")
    private BusinessRole role;

    public MembershipCreateRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BusinessRole getRole() {
        return role;
    }

    public void setRole(BusinessRole role) {
        this.role = role;
    }
}
