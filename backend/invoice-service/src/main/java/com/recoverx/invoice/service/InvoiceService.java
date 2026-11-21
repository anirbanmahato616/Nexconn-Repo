package com.recoverx.invoice.service;

import java.util.List;

import com.recoverx.invoice.dto.InvoiceRequest;
import com.recoverx.invoice.dto.InvoiceResponse;
import java.math.BigDecimal;

public interface InvoiceService {

    InvoiceResponse createInvoice(Long businessId, InvoiceRequest request);

    List<InvoiceResponse> getAllInvoices(Long businessId);

    InvoiceResponse getInvoiceById(Long businessId, Long invoiceId);

    InvoiceResponse recordVerifiedPayment(Long businessId, Long invoiceId, Long paymentId, BigDecimal amount);

    InvoiceResponse updateInvoice(
            Long businessId, Long invoiceId, InvoiceRequest request);

List<InvoiceResponse> getMyInvoices(String authorizationHeader);

InvoiceResponse getMyInvoiceById(
        Long invoiceId, String authorizationHeader);

}
