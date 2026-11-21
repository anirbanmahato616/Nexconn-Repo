package com.recoverx.invoice.utility;

import com.recoverx.invoice.dto.InvoiceRequest;
import com.recoverx.invoice.dto.InvoiceResponse;
import com.recoverx.invoice.entity.Invoice;

public final class InvoiceMapper {

    private InvoiceMapper() {
    }

    public static Invoice toEntity(Long businessId, InvoiceRequest request) {
        Invoice invoice = new Invoice();
        invoice.setBusinessId(businessId);
        invoice.setCustomerId(request.getCustomerId());
        invoice.setInvoiceNumber(request.getInvoiceNumber().trim());
        invoice.setDescription(request.getDescription());
        invoice.setAmount(request.getAmount());
        invoice.setIssueDate(request.getIssueDate());
        invoice.setDueDate(request.getDueDate());
        return invoice;
    }

    public static InvoiceResponse toResponse(Invoice invoice) {
        InvoiceResponse response = new InvoiceResponse();
        response.setId(invoice.getId());
        response.setBusinessId(invoice.getBusinessId());
        response.setCustomerId(invoice.getCustomerId());
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setDescription(invoice.getDescription());
        response.setAmount(invoice.getAmount());
        response.setIssueDate(invoice.getIssueDate());
        response.setDueDate(invoice.getDueDate());
        response.setStatus(invoice.getStatus());
        response.setPaidAmount(invoice.getPaidAmount());
        response.setOutstandingAmount(invoice.getAmount().subtract(invoice.getPaidAmount()));
        return response;
    }
}
