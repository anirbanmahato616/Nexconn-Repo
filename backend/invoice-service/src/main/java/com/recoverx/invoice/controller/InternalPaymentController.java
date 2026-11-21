package com.recoverx.invoice.controller;

import com.recoverx.invoice.dto.InvoiceResponse;
import com.recoverx.invoice.dto.VerifiedPaymentRequest;
import com.recoverx.invoice.service.InvoiceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/businesses/{businessId}/invoices/{invoiceId}")
@Validated
public class InternalPaymentController {
    private final InvoiceService invoiceService;
    private final String paymentServiceSecret;

    public InternalPaymentController(InvoiceService invoiceService,
            @Value("${internal.payment-service.secret}") String paymentServiceSecret) {
        this.invoiceService = invoiceService;
        this.paymentServiceSecret = paymentServiceSecret;
    }

    @GetMapping("/details")
    public ResponseEntity<InvoiceResponse> getInvoiceDetails(
            @PathVariable @Positive Long businessId,
            @PathVariable @Positive Long invoiceId,
            @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey) {
        if (!isValidKey(serviceKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(invoiceService.getInvoiceById(businessId, invoiceId));
    }

    @PatchMapping("/verified-payments")
    public ResponseEntity<InvoiceResponse> recordVerifiedPayment(
            @PathVariable @Positive Long businessId,
            @PathVariable @Positive Long invoiceId,
            @Valid @RequestBody VerifiedPaymentRequest request,
            @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey) {
        if (!isValidKey(serviceKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(invoiceService.recordVerifiedPayment(
                businessId, invoiceId, request.getPaymentId(), request.getAmount()));
    }

    private boolean isValidKey(String serviceKey) {
        return StringUtils.hasText(serviceKey)
                && MessageDigest.isEqual(
                    paymentServiceSecret.getBytes(StandardCharsets.UTF_8),
                    serviceKey.getBytes(StandardCharsets.UTF_8));
    }
}
