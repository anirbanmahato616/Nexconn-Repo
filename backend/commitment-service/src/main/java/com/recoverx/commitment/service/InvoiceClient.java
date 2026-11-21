package com.recoverx.commitment.service;

import com.recoverx.commitment.dto.InvoiceDetails;
import com.recoverx.commitment.exception.InvoiceIntegrationException;
import com.recoverx.commitment.security.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;

@Component
public class InvoiceClient {
    private final RestClient client;

    public InvoiceClient(RestClient.Builder builder,
            @Value("${services.invoice.base-url:http://localhost:8082}") String baseUrl) {
        client = builder.clone().baseUrl(baseUrl).build();
    }

    public InvoiceDetails getInvoice(Long businessId, Long invoiceId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
                throw new InvoiceIntegrationException("Authenticated user context is missing for invoice validation");
            }

            InvoiceDetails details = client.get()
                .uri("/api/businesses/{businessId}/invoices/{invoiceId}", businessId, invoiceId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.getToken())
                .retrieve()
                .body(InvoiceDetails.class);

            if (details == null || details.getId() == null || details.getBusinessId() == null) {
                throw new InvoiceIntegrationException("Invoice Service returned incomplete invoice data");
            }
            return details;
        } catch (RestClientException ex) {
            throw new InvoiceIntegrationException(
                    "Unable to validate invoice through Invoice Service: " + ex.getMessage(), ex);
        }
    }
    
    public List<InvoiceDetails> getMyInvoices() {
        try {
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null ||
                    !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
                throw new InvoiceIntegrationException(
                        "Authenticated user context is missing");
            }

            List<InvoiceDetails> invoices = client.get()
                    .uri("/api/customers/me/invoices")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.getToken())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<InvoiceDetails>>() {});

            return invoices == null ? List.of() : invoices;

        } catch (RestClientException ex) {
            throw new InvoiceIntegrationException(
                    "Unable to retrieve customer invoices: " + ex.getMessage(), ex);
        }
    }
}
