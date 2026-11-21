
package com.recoverx.customer.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recoverx.customer.dto.CustomerResponse;
import com.recoverx.customer.security.AuthenticatedUser;
import com.recoverx.customer.service.CustomerService;

@RestController
@RequestMapping("/api/customers")
public class MyCustomerController {

    private final CustomerService customerService;

    public MyCustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<CustomerResponse>> getMyConnections(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        return ResponseEntity.ok(
                customerService.getMyCustomerConnections(currentUser.getUserId()));
    }
}
