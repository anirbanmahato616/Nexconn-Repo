package com.recoverx.customer.service;

import java.util.List;

import com.recoverx.customer.dto.CustomerRequest;
import com.recoverx.customer.dto.CustomerResponse;

public interface CustomerService {

    CustomerResponse createCustomer(Long businessId, CustomerRequest request);

    List<CustomerResponse> getAllCustomers(Long businessId);

    CustomerResponse getCustomerById(Long businessId, Long customerId);

    CustomerResponse updateCustomer(
            Long businessId, Long customerId, CustomerRequest request);
    List<CustomerResponse> getMyCustomerConnections(Long userId);
}
