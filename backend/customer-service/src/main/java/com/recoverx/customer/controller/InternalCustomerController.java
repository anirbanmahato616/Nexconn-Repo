package com.recoverx.customer.controller;

import com.recoverx.customer.dto.CustomerResponse;
import com.recoverx.customer.service.CustomerService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/internal/businesses/{businessId}/customers")
@Validated
public class InternalCustomerController {
    private final CustomerService customerService;
    private final String paymentServiceSecret;

    public InternalCustomerController(CustomerService customerService,
            @Value("${internal.payment-service.secret}") String paymentServiceSecret) {
        this.customerService = customerService;
        this.paymentServiceSecret = paymentServiceSecret;
    }

    @GetMapping("/{customerId}/user/{userId}")
    public ResponseEntity<CustomerResponse> verifyCustomerOwnership(
            @PathVariable @Positive Long businessId,
            @PathVariable @Positive Long customerId,
            @PathVariable @Positive Long userId,
            @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey) {
        if (!StringUtils.hasText(serviceKey)
                || !MessageDigest.isEqual(
                    paymentServiceSecret.getBytes(StandardCharsets.UTF_8),
                    serviceKey.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CustomerResponse customer = customerService.getCustomerById(businessId, customerId);
        if (customer.getUserId() == null || !userId.equals(customer.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(customer);
    }
}
