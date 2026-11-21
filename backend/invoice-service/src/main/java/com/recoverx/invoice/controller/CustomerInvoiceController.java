
package com.recoverx.invoice.controller;

import java.util.List;

import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.recoverx.invoice.dto.InvoiceResponse;
import com.recoverx.invoice.service.InvoiceService;

@RestController
@RequestMapping("/api/customers/me/invoices")
@Validated
public class CustomerInvoiceController {

    private final InvoiceService invoiceService;

    public CustomerInvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> getMyInvoices(
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorization) {

        return ResponseEntity.ok(
                invoiceService.getMyInvoices(authorization));
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<InvoiceResponse> getMyInvoiceById(
            @PathVariable @Positive Long invoiceId,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorization) {

        return ResponseEntity.ok(
                invoiceService.getMyInvoiceById(
                        invoiceId, authorization));
    }
}
