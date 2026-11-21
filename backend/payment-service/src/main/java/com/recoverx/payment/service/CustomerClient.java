package com.recoverx.payment.service;

import com.recoverx.payment.exception.InvoiceIntegrationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CustomerClient {
    private final RestClient restClient;
    private final String paymentServiceSecret;

    public CustomerClient(RestClient.Builder builder,
            @Value("${services.customer.base-url:http://localhost:8081}") String baseUrl,
            @Value("${internal.payment-service.secret}") String paymentServiceSecret) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.paymentServiceSecret = paymentServiceSecret;
    }

    public void requireCustomerOwnsRecord(Long businessId, Long customerId, Long userId) {
        try {
            restClient.get()
                .uri("/internal/businesses/{businessId}/customers/{customerId}/user/{userId}",
                        businessId, customerId, userId)
                .header("X-Internal-Service-Key", paymentServiceSecret)
                .retrieve()
                .toBodilessEntity();
        } catch (org.springframework.web.client.HttpClientErrorException.Forbidden ex) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You can submit payments only for invoices assigned to your RecoverX account");
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound ex) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "The customer record for this invoice could not be verified");
        } catch (RestClientException ex) {
            throw new InvoiceIntegrationException(
                "Could not verify customer ownership. Ensure Customer Service is running and its internal endpoint is available.", ex);
        }
    }
}
