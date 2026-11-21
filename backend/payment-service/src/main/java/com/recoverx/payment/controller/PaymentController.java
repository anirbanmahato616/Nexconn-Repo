package com.recoverx.payment.controller;

import com.recoverx.payment.dto.*;
import com.recoverx.payment.security.AuthenticatedUser;
import com.recoverx.payment.service.BusinessMembershipClient;
import com.recoverx.payment.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/businesses/{businessId}/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final BusinessMembershipClient membershipClient;

    public PaymentController(PaymentService paymentService, BusinessMembershipClient membershipClient) {
        this.paymentService = paymentService;
        this.membershipClient = membershipClient;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @PathVariable @Positive Long businessId,
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        // Customer ownership is checked against the invoice's linked customer record
        // inside PaymentService. Business membership is not required to submit a claim.
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPayment(businessId, request, user.getUserId()));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments(
            @PathVariable @Positive Long businessId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        membershipClient.requireActiveMembership(businessId, user.getUserId());
        return ResponseEntity.ok(paymentService.getAllPayments(businessId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable @Positive Long businessId, @PathVariable @Positive Long id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        membershipClient.requireActiveMembership(businessId, user.getUserId());
        return ResponseEntity.ok(paymentService.getPaymentById(businessId, id));
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByInvoice(
            @PathVariable @Positive Long businessId, @PathVariable @Positive Long invoiceId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        membershipClient.requireActiveMembership(businessId, user.getUserId());
        return ResponseEntity.ok(paymentService.getPaymentsByInvoice(businessId, invoiceId));
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @PathVariable @Positive Long businessId, @PathVariable @Positive Long id,
            @Valid @RequestBody PaymentReviewRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        BusinessMembershipDetails membership = membershipClient.requireActiveMembership(businessId, user.getUserId());
        membershipClient.requirePaymentReviewer(membership);
        return ResponseEntity.ok(paymentService.verifyPayment(businessId, id, request, user.getUserId()));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<PaymentResponse> rejectPayment(
            @PathVariable @Positive Long businessId, @PathVariable @Positive Long id,
            @Valid @RequestBody PaymentReviewRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        BusinessMembershipDetails membership = membershipClient.requireActiveMembership(businessId, user.getUserId());
        membershipClient.requirePaymentReviewer(membership);
        return ResponseEntity.ok(paymentService.rejectPayment(businessId, id, request, user.getUserId()));
    }
}
