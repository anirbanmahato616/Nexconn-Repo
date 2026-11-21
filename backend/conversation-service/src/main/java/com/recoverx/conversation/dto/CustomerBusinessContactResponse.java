package com.recoverx.conversation.dto;

public class CustomerBusinessContactResponse {

    private Long businessId;
    private String businessName;
    private String businessEmail;

    public CustomerBusinessContactResponse() {
    }

    public CustomerBusinessContactResponse(
            Long businessId,
            String businessName,
            String businessEmail) {
        this.businessId = businessId;
        this.businessName = businessName;
        this.businessEmail = businessEmail;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

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
}