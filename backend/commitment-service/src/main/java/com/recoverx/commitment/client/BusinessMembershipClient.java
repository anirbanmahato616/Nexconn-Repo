package com.recoverx.commitment.client;

import com.recoverx.commitment.exception.BusinessRuleException;
import com.recoverx.commitment.exception.InvoiceIntegrationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class BusinessMembershipClient {
    private final RestClient client;

    public BusinessMembershipClient(RestClient.Builder builder,
            @Value("${services.business.base-url:http://localhost:8088}") String baseUrl) {
        this.client = builder.clone().baseUrl(baseUrl).build();
    }

    public MembershipDetails getMembership(Long businessId, Long userId, String token) {
        try {
            MembershipDetails result = client.get()
                .uri("/api/businesses/{businessId}/members/{userId}", businessId, userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(MembershipDetails.class);
            if (result == null) {
                throw new org.springframework.security.access.AccessDeniedException("Business membership could not be verified");
            }
            return result;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 403 || ex.getStatusCode().value() == 404) {
                throw new org.springframework.security.access.AccessDeniedException("You are not an active member of this business");
            }
            throw new InvoiceIntegrationException("Unable to verify business membership: " + ex.getStatusText(), ex);
        } catch (org.springframework.web.client.RestClientException ex) {
            throw new InvoiceIntegrationException("Business Service is unavailable while checking membership", ex);
        }
    }

    public static class MembershipDetails {
        private Long businessId;
        private Long userId;
        private String role;
        private boolean active;

        public Long getBusinessId() { return businessId; }
        public void setBusinessId(Long businessId) { this.businessId = businessId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }
}
