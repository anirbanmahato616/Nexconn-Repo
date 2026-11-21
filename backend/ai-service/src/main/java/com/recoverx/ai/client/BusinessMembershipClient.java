package com.recoverx.ai.client;

import com.recoverx.ai.security.AuthenticatedUser;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Component
public class BusinessMembershipClient {
    private final RestClient restClient;

    public BusinessMembershipClient(RestClient.Builder builder,
                                    @Value("${business.service.url}") String businessServiceUrl) {
        this.restClient = builder.baseUrl(businessServiceUrl).build();
    }

    @SuppressWarnings("unchecked")
    public void requireActiveMembership(Long businessId, AuthenticatedUser user, String authorizationHeader) {
        if (businessId == null || businessId <= 0) {
            throw new IllegalArgumentException("A valid businessId is required for AI analysis");
        }

        try {
            Map<String, Object> membership = restClient.get()
                    .uri("/api/businesses/{businessId}/members/{userId}", businessId, user.getUserId())
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .body(Map.class);

            if (membership == null || !Boolean.TRUE.equals(membership.get("active"))) {
                throw new AccessDeniedException("You are not an active member of this business");
            }
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 401) {
                throw new org.springframework.security.authentication.BadCredentialsException(
                        "Business Service rejected the JWT", ex);
            }
            if (ex.getStatusCode().value() == 403 || ex.getStatusCode().value() == 404) {
                throw new AccessDeniedException("You are not an active member of this business", ex);
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Business Service is currently unavailable", ex);
        } catch (org.springframework.web.client.ResourceAccessException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Business Service is currently unavailable", ex);
        }
    }
}
