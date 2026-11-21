
package com.recoverx.invoice.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import com.recoverx.invoice.dto.CustomerConnectionResponse;

@Component
public class CustomerConnectionClient {

    private final RestClient restClient;

    public CustomerConnectionClient(
            @Value("${customer.service.url:http://localhost:8081}")
            String customerServiceUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(customerServiceUrl)
                .build();
    }

    public List<CustomerConnectionResponse> getMyConnections(
            String authorizationHeader) {

        try {
            List<CustomerConnectionResponse> connections = restClient.get()
                    .uri("/api/customers/me")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .body(new ParameterizedTypeReference<
                            List<CustomerConnectionResponse>>() {});

            return connections == null ? List.of() : connections;

        } catch (RestClientResponseException ex) {
            throw new ResponseStatusException(
                    ex.getStatusCode(),
                    "Customer Service rejected the customer connection request");

        } catch (ResourceAccessException ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Customer Service is unavailable");
        }
    }
}
