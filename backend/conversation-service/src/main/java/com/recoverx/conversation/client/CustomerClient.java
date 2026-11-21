package com.recoverx.conversation.client;

import com.recoverx.conversation.dto.CustomerDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;

@Component
public class CustomerClient {
    private final RestClient restClient;
    public CustomerClient(RestClient.Builder builder,
            @Value("${services.customer.base-url:http://localhost:8081}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }
    public CustomerDetails getCustomer(Long businessId, Long customerId, String authorization) {
        try {
            CustomerDetails customer = restClient.get()
                .uri("/api/businesses/{businessId}/customers/{customerId}", businessId, customerId)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve().body(CustomerDetails.class);
            if (customer == null || customer.getId() == null || customer.getBusinessId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Customer Service returned incomplete customer data");
            }
            return customer;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer does not exist in this business");
        } catch (HttpClientErrorException.Forbidden | HttpClientErrorException.Unauthorized ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer record could not be accessed");
        } catch (ResourceAccessException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Customer Service is unavailable");
        }
    }
    
    public List<CustomerDetails> getMyConnections(String authorization) {
        try {
            List<CustomerDetails> customers = restClient.get()
                    .uri("/api/customers/me")
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<CustomerDetails>>() {});

            return customers != null ? customers : List.of();

        } catch (ResourceAccessException ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Customer Service is unavailable");
        }
    }
}
