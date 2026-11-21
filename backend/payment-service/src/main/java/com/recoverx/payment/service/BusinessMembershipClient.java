package com.recoverx.payment.service;

import com.recoverx.payment.dto.BusinessMembershipDetails;
import com.recoverx.payment.exception.InvoiceIntegrationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class BusinessMembershipClient {
    private final RestClient restClient;
    private final HttpServletRequest request;

    public BusinessMembershipClient(RestClient.Builder builder,
            @Value("${services.business.base-url:http://localhost:8088}") String baseUrl,
            HttpServletRequest request) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.request = request;
    }

    public BusinessMembershipDetails requireActiveMembership(Long businessId, Long userId) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new AccessDeniedException("Missing bearer token for membership verification");
        }
        try {
            BusinessMembershipDetails membership = restClient.get()
                .uri("/api/businesses/{businessId}/members/{userId}", businessId, userId)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(BusinessMembershipDetails.class);

            if (membership == null || !membership.isActive()
                    || !businessId.equals(membership.getBusinessId())
                    || !userId.equals(membership.getUserId())) {
                throw new AccessDeniedException("You are not an active member of this business");
            }
            return membership;
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound ex) {
            throw new AccessDeniedException("You are not an active member of this business");
        } catch (org.springframework.web.client.HttpClientErrorException.Forbidden ex) {
            throw new AccessDeniedException("You are not authorized for this business");
        } catch (RestClientException ex) {
            throw new InvoiceIntegrationException(
                "Could not verify business membership. Ensure Business Service is running and its membership endpoint is available.", ex);
        }
    }

    public void requirePaymentReviewer(BusinessMembershipDetails membership) {
        String role = membership.getRole() == null ? "" : membership.getRole().toUpperCase();
        if (!"OWNER".equals(role) && !"ADMIN".equals(role)) {
            throw new AccessDeniedException("Only an OWNER or ADMIN can verify or reject payments");
        }
    }
}
