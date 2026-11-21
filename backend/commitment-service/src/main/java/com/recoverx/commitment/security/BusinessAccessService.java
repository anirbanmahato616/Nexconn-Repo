package com.recoverx.commitment.security;

import com.recoverx.commitment.client.BusinessMembershipClient;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class BusinessAccessService {
    private final BusinessMembershipClient membershipClient;

    public BusinessAccessService(BusinessMembershipClient membershipClient) {
        this.membershipClient = membershipClient;
    }

    public AuthenticatedUser requireActiveMember(Long businessId) {
        AuthenticatedUser user = currentUser();
        BusinessMembershipClient.MembershipDetails membership =
                membershipClient.getMembership(businessId, user.getUserId(), user.getToken());
        if (!membership.isActive()) {
            throw new AccessDeniedException("You are not an active member of this business");
        }
        if (membership.getUserId() != null && !user.getUserId().equals(membership.getUserId())) {
            throw new AccessDeniedException("Business Service returned a mismatched membership");
        }
        return user;
    }

    public AuthenticatedUser requireOwnerOrAdmin(Long businessId) {
        AuthenticatedUser user = currentUser();
        BusinessMembershipClient.MembershipDetails membership =
                membershipClient.getMembership(businessId, user.getUserId(), user.getToken());
        if (!membership.isActive()) {
            throw new AccessDeniedException("You are not an active member of this business");
        }
        if (membership.getUserId() != null && !user.getUserId().equals(membership.getUserId())) {
            throw new AccessDeniedException("Business Service returned a mismatched membership");
        }
        String role = membership.getRole();
        if (!"OWNER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            throw new AccessDeniedException("Only OWNER or ADMIN can review commitments or manage installments");
        }
        return user;
    }

    private AuthenticatedUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new AccessDeniedException("Authentication is required");
        }
        return user;
    }
}
