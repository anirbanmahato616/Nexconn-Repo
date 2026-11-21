package com.recoverx.conversation.dto;

public class MembershipResponse {
    private Long id;
    private Long businessId;
    private Long userId;
    private String role;
    private Boolean active;

    public Long getId() { return id; }
    public Long getBusinessId() { return businessId; }
    public Long getUserId() { return userId; }
    public String getRole() { return role; }
    public Boolean getActive() { return active; }
}
