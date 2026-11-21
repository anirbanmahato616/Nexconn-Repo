
package com.recoverx.payment.controller;

import com.recoverx.payment.dto.PaymentRequest;
import com.recoverx.payment.dto.PaymentResponse;
import com.recoverx.payment.security.AuthenticatedUser;
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
@RequestMapping("/api/customers/me/payments")
public class CustomerPaymentController {

    private final PaymentService paymentService;

    public CustomerPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Customer views their own payment history
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getMyPayments(
            @AuthenticationPrincipal AuthenticatedUser user) {

        return ResponseEntity.ok(
                paymentService.getMyPayments(user.getUserId())
        );
    }

    // Customer submits a payment claim for an invoice
    @PostMapping("/{businessId}")
    public ResponseEntity<PaymentResponse> submitPaymentClaim(
            @PathVariable @Positive Long businessId,
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        PaymentResponse response = paymentService.createPayment(
                businessId,
                request,
                user.getUserId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
