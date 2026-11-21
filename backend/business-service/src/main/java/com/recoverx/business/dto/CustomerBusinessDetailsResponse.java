package com.recoverx.business.dto;

public class CustomerBusinessDetailsResponse {

    private Long id;
    private String name;
    private String email;
    private Long ownerUserId;

    public CustomerBusinessDetailsResponse() {
    }

    public CustomerBusinessDetailsResponse(
            Long id,
            String name,
            String email,
            Long ownerUserId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.ownerUserId = ownerUserId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }
}