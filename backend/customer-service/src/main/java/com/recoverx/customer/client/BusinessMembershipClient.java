package com.recoverx.customer.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.recoverx.customer.dto.MembershipResponse;

@Component
public class BusinessMembershipClient {
    private final RestClient restClient;

    public BusinessMembershipClient(
            @Value("${business.service.url}") String businessServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(businessServiceUrl)
                .build();
    }

    public void requireActiveMembership(Long businessId, Long userId,
            String authorizationHeader) {
        getActiveMembership(businessId, userId, authorizationHeader);
    }

    public void requireInvitationPermission(Long businessId, Long userId,
            String authorizationHeader) {
        MembershipResponse membership =
                getActiveMembership(businessId, userId, authorizationHeader);
        String role = membership.getRole() == null
                ? "" : membership.getRole().toUpperCase(java.util.Locale.ROOT);
        if (!"OWNER".equals(role) && !"ADMIN".equals(role)) {
            throw new AccessDeniedException(
                    "Only an OWNER or ADMIN can invite customers");
        }
    }

    private MembershipResponse getActiveMembership(Long businessId, Long userId,
            String authorizationHeader) {
        try {
            MembershipResponse membership = restClient.get()
                    .uri("/api/businesses/{businessId}/members/{userId}",
                            businessId, userId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .body(MembershipResponse.class);

            if (membership == null
                    || !Boolean.TRUE.equals(membership.getActive())
                    || !businessId.equals(membership.getBusinessId())
                    || !userId.equals(membership.getUserId())) {
                throw new AccessDeniedException(
                        "You are not an active member of this business");
            }
            return membership;
        } catch (HttpClientErrorException.Unauthorized
                | HttpClientErrorException.Forbidden
                | HttpClientErrorException.NotFound ex) {
            throw new AccessDeniedException(
                    "You are not an active member of this business");
        } catch (ResourceAccessException ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Business Service is unavailable; membership could not be verified");
        }
    }
}
