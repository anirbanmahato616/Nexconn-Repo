
package com.recoverx.business.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.recoverx.business.dto.CustomerBusinessDetailsResponse;
import com.recoverx.business.service.BusinessService;

@RestController
@RequestMapping("/api/internal/businesses")
public class InternalBusinessController {

    private final BusinessService businessService;

    public InternalBusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping("/customer-details")
    public ResponseEntity<CustomerBusinessDetailsResponse> getCustomerBusinessDetails(
            @RequestParam Long businessId) {

        return ResponseEntity.ok(
                businessService.getCustomerBusinessDetails(businessId)
        );
    }
}
