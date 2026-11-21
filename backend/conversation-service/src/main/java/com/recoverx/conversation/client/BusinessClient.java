
package com.recoverx.conversation.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.recoverx.conversation.dto.BusinessDetails;

@Component
public class BusinessClient {

    private final RestClient restClient;
    private final String internalApiKey;

    public BusinessClient(
            RestClient.Builder builder,
            @Value("${business.service.url:http://localhost:8088}") String baseUrl,
            @Value("${recoverx.internal-api-key}") String internalApiKey) {

        this.restClient = builder.baseUrl(baseUrl).build();
        this.internalApiKey = internalApiKey;
    }

    public BusinessDetails getCustomerBusinessDetails(Long businessId) {

        try {
            BusinessDetails business = restClient.get()
                    .uri("/api/internal/businesses/customer-details?businessId={businessId}",
                            businessId)
                    .header("X-Internal-API-Key", internalApiKey)
                    .retrieve()
                    .body(BusinessDetails.class);

            if (business == null || business.getId() == null
                    || business.getName() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Business Service returned incomplete business details");
            }

            return business;

        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not retrieve business details from Business Service",
                    ex);
        }
    }
}
