package com.recoverx.customer.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.recoverx.customer.client.BusinessMembershipClient;
import com.recoverx.customer.dto.CustomerRequest;
import com.recoverx.customer.dto.CustomerResponse;
import com.recoverx.customer.security.AuthenticatedUser;
import com.recoverx.customer.service.CustomerService;

@RestController
@RequestMapping("/api/businesses/{businessId}/customers")
@Validated
public class CustomerController {

    private final CustomerService customerService;
    private final BusinessMembershipClient businessMembershipClient;

    public CustomerController(CustomerService customerService,
            BusinessMembershipClient businessMembershipClient) {
        this.customerService = customerService;
        this.businessMembershipClient = businessMembershipClient;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @PathVariable @Positive Long businessId,
            @Valid @RequestBody CustomerRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        authorize(businessId, currentUser, authorization);
        CustomerResponse response = customerService.createCustomer(businessId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers(
            @PathVariable @Positive Long businessId,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        authorize(businessId, currentUser, authorization);
        return ResponseEntity.ok(customerService.getAllCustomers(businessId));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomerById(
            @PathVariable @Positive Long businessId,
            @PathVariable @Positive Long customerId,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        authorize(businessId, currentUser, authorization);
        return ResponseEntity.ok(
                customerService.getCustomerById(businessId, customerId));
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable @Positive Long businessId,
            @PathVariable @Positive Long customerId,
            @Valid @RequestBody CustomerRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        authorize(businessId, currentUser, authorization);
        return ResponseEntity.ok(
                customerService.updateCustomer(businessId, customerId, request));
    }

    private void authorize(Long businessId, AuthenticatedUser currentUser,
            String authorization) {
        businessMembershipClient.requireActiveMembership(
                businessId, currentUser.getUserId(), authorization);
    }
}
