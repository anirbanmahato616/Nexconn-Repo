package com.recoverx.business.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.recoverx.business.dto.BusinessCreateRequest;
import com.recoverx.business.dto.BusinessResponse;
import com.recoverx.business.dto.BusinessUpdateRequest;
import com.recoverx.business.security.AuthenticatedUser;
import com.recoverx.business.service.BusinessService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/businesses")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @PostMapping
    public ResponseEntity<BusinessResponse> createBusiness(
            @Valid @RequestBody BusinessCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        BusinessResponse response = businessService.createBusiness(
                request, currentUser.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // A user can only retrieve their own owned businesses.
    @GetMapping("/owner/me")
    public ResponseEntity<List<BusinessResponse>> getMyBusinesses(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        return ResponseEntity.ok(
                businessService.getBusinessesByOwner(currentUser.getUserId()));
    }

    @GetMapping("/{businessId}")
    public ResponseEntity<BusinessResponse> getBusinessById(
            @PathVariable Long businessId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        return ResponseEntity.ok(
                businessService.getBusinessById(
                        businessId, currentUser.getUserId()));
    }

    @PutMapping("/{businessId}")
    public ResponseEntity<BusinessResponse> updateBusiness(
            @PathVariable Long businessId,
            @Valid @RequestBody BusinessUpdateRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        return ResponseEntity.ok(
                businessService.updateBusiness(
                        businessId, request, currentUser.getUserId()));
    }

    @DeleteMapping("/{businessId}")
    public ResponseEntity<Void> deactivateBusiness(
            @PathVariable Long businessId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        businessService.deactivateBusiness(
                businessId, currentUser.getUserId());

        return ResponseEntity.noContent().build();
    }
}
