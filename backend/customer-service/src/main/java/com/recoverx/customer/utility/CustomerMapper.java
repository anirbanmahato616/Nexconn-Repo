package com.recoverx.customer.utility;

import com.recoverx.customer.dto.CustomerRequest;
import com.recoverx.customer.dto.CustomerResponse;
import com.recoverx.customer.entity.Customer;

public final class CustomerMapper {

    private CustomerMapper() {
    }

    public static Customer toEntity(Long businessId, CustomerRequest request) {
        Customer customer = new Customer();
        customer.setBusinessId(businessId);
        customer.setName(request.getName().trim());
        customer.setEmail(request.getEmail().trim().toLowerCase());
        customer.setPhone(
                request.getPhone() == null || request.getPhone().isBlank()
                        ? null : request.getPhone().trim());
        return customer;
    }

    public static CustomerResponse toResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setBusinessId(customer.getBusinessId());
        response.setUserId(customer.getUserId());
        response.setName(customer.getName());
        response.setEmail(customer.getEmail());
        response.setPhone(customer.getPhone());
        response.setCreatedAt(customer.getCreatedAt());
        return response;
    }
}
