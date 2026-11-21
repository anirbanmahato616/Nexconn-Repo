package com.recoverx.conversation.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.recoverx.conversation.dto.MembershipResponse;

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
