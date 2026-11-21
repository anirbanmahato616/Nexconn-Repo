package com.recoverx.payment.service;

import com.recoverx.payment.dto.InvoiceDetails;
import java.math.BigDecimal;
import java.util.Map;
import com.recoverx.payment.exception.InvoiceIntegrationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class InvoiceClient {
    private final RestClient restClient;
    private final String paymentServiceSecret;

    public InvoiceClient(RestClient.Builder builder,
            @Value("${services.invoice.base-url:http://localhost:8082}") String baseUrl,
            @Value("${internal.payment-service.secret}") String paymentServiceSecret) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.paymentServiceSecret = paymentServiceSecret;
    }

    public InvoiceDetails recordVerifiedPayment(Long businessId, Long invoiceId,
            Long paymentId, BigDecimal amount) {
        try {
            InvoiceDetails invoice = restClient.patch()
                .uri("/internal/businesses/{businessId}/invoices/{invoiceId}/verified-payments",
                        businessId, invoiceId)
                .header("X-Internal-Service-Key", paymentServiceSecret)
                .body(Map.of("paymentId", paymentId, "amount", amount))
                .retrieve()
                .body(InvoiceDetails.class);
            if (invoice == null) {
                throw new InvoiceIntegrationException("Invoice Service returned an empty payment update response");
            }
            return invoice;
        } catch (RestClientException ex) {
            throw new InvoiceIntegrationException(
                "Could not update the invoice after payment verification: " + ex.getMessage(), ex);
        }
    }

    public InvoiceDetails getInvoice(Long businessId, Long invoiceId) {
        try {
            InvoiceDetails invoice = restClient.get()
                .uri("/internal/businesses/{businessId}/invoices/{invoiceId}/details",
                        businessId, invoiceId)
                .header("X-Internal-Service-Key", paymentServiceSecret)
                .retrieve().body(InvoiceDetails.class);
            if (invoice == null || invoice.getId() == null || invoice.getAmount() == null
                    || invoice.getCustomerId() == null) {
                throw new InvoiceIntegrationException("Invoice Service returned incomplete invoice data");
            }
            return invoice;
        } catch (RestClientException ex) {
            throw new InvoiceIntegrationException(
                "Could not validate invoice. Ensure Invoice Service is running and its internal endpoint is available. " + ex.getMessage(), ex);
        }
    }
}
