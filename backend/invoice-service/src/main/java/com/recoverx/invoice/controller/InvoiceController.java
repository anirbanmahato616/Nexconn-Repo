package com.recoverx.invoice.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recoverx.invoice.client.BusinessMembershipClient;
import com.recoverx.invoice.security.AuthenticatedUser;
import com.recoverx.invoice.dto.InvoiceRequest;
import com.recoverx.invoice.dto.InvoiceResponse;
import com.recoverx.invoice.service.InvoiceService;

@RestController
@RequestMapping("/api/businesses/{businessId}/invoices")
@Validated
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final BusinessMembershipClient businessMembershipClient;

    public InvoiceController(InvoiceService invoiceService,
            BusinessMembershipClient businessMembershipClient) {
        this.invoiceService = invoiceService;
        this.businessMembershipClient = businessMembershipClient;
    }

    @PostMapping
    public ResponseEntity<InvoiceResponse> createInvoice(
            @PathVariable @Positive Long businessId,
            @Valid @RequestBody InvoiceRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        authorize(businessId, currentUser, authorization);
        InvoiceResponse response = invoiceService.createInvoice(businessId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> getAllInvoices(
            @PathVariable @Positive Long businessId,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        authorize(businessId, currentUser, authorization);
        return ResponseEntity.ok(invoiceService.getAllInvoices(businessId));
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<InvoiceResponse> getInvoiceById(
            @PathVariable @Positive Long businessId,
            @PathVariable @Positive Long invoiceId,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        authorize(businessId, currentUser, authorization);
        return ResponseEntity.ok(
                invoiceService.getInvoiceById(businessId, invoiceId));
    }

    @PutMapping("/{invoiceId}")
    public ResponseEntity<InvoiceResponse> updateInvoice(
            @PathVariable @Positive Long businessId,
            @PathVariable @Positive Long invoiceId,
            @Valid @RequestBody InvoiceRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {

        authorize(businessId, currentUser, authorization);
        return ResponseEntity.ok(
                invoiceService.updateInvoice(businessId, invoiceId, request));
    }

    private void authorize(Long businessId, AuthenticatedUser currentUser,
            String authorization) {
        businessMembershipClient.requireActiveMembership(
                businessId, currentUser.getUserId(), authorization);
    }
}
