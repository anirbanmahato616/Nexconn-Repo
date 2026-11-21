package com.recoverx.conversation.service;

import com.recoverx.conversation.dto.InvoiceDetails;
import com.recoverx.conversation.exception.InvoiceIntegrationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class InvoiceClient {
    private final RestClient restClient;
    public InvoiceClient(RestClient.Builder builder,
            @Value("${services.invoice.base-url:http://localhost:8082}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }
    public InvoiceDetails getInvoice(Long businessId, Long invoiceId, String authorizationHeader) {
        try {
            InvoiceDetails invoice = restClient.get()
                .uri("/api/businesses/{businessId}/invoices/{invoiceId}", businessId, invoiceId)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .retrieve().body(InvoiceDetails.class);
            if (invoice == null || invoice.getId() == null || invoice.getBusinessId() == null) {
                throw new InvoiceIntegrationException("Invoice Service returned incomplete invoice data");
            }
            return invoice;
        } catch (RestClientException ex) {
            throw new InvoiceIntegrationException(
                "Could not validate invoice. Check Invoice Service availability and endpoint contract: " + ex.getMessage(), ex);
        }
    }
}
