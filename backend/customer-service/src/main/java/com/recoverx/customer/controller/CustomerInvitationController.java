package com.recoverx.customer.controller;

import com.recoverx.customer.dto.AcceptInvitationRequest;
import com.recoverx.customer.dto.InvitationResponse;
import com.recoverx.customer.security.AuthenticatedUser;
import com.recoverx.customer.service.CustomerInvitationService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/businesses/{businessId}/customers")
public class CustomerInvitationController {

    private final CustomerInvitationService invitationService;

    public CustomerInvitationController(CustomerInvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping("/{customerId}/invite")
    public ResponseEntity<InvitationResponse> inviteCustomer(
            @PathVariable @Positive Long businessId,
            @PathVariable @Positive Long customerId,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        InvitationResponse response = invitationService.inviteCustomer(
                businessId, customerId, currentUser.getUserId(), authorization);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/invitations/accept")
    public ResponseEntity<InvitationResponse> acceptInvitation(
            @PathVariable @Positive Long businessId,
            @Valid @RequestBody AcceptInvitationRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        InvitationResponse response = invitationService.acceptInvitation(
                businessId, request, currentUser.getUserId(), currentUser.getEmail());
        return ResponseEntity.ok(response);
    }
}
