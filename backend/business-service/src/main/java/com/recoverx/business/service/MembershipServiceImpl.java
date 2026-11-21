package com.recoverx.business.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recoverx.business.dto.MembershipCreateRequest;
import com.recoverx.business.dto.MembershipResponse;
import com.recoverx.business.entity.Business;
import com.recoverx.business.entity.BusinessMembership;
import com.recoverx.business.entity.BusinessRole;
import com.recoverx.business.repository.BusinessMembershipRepository;
import com.recoverx.business.repository.BusinessRepository;

@Service
public class MembershipServiceImpl implements MembershipService {

    private final BusinessMembershipRepository membershipRepository;
    private final BusinessRepository businessRepository;

    public MembershipServiceImpl(
            BusinessMembershipRepository membershipRepository,
            BusinessRepository businessRepository) {
        this.membershipRepository = membershipRepository;
        this.businessRepository = businessRepository;
    }

    @Override
    @Transactional
    public MembershipResponse addMember(
            Long businessId, MembershipCreateRequest request, Long actorUserId) {

        getActiveBusiness(businessId);
        BusinessRole actorRole = requireManager(businessId, actorUserId);
        BusinessRole requestedRole = request.getRole();

        if (requestedRole != BusinessRole.ADMIN
                && requestedRole != BusinessRole.STAFF) {
            throw new IllegalArgumentException(
                    "Only ADMIN or STAFF roles can be assigned");
        }

        if (actorRole == BusinessRole.ADMIN && requestedRole != BusinessRole.STAFF) {
            throw new AccessDeniedException(
                    "An ADMIN can only add STAFF members");
        }

        if (request.getUserId().equals(actorUserId)) {
            throw new IllegalArgumentException(
                    "You are already a member of this business");
        }

        BusinessMembership membership = membershipRepository
                .findByBusinessIdAndUserId(businessId, request.getUserId())
                .orElse(null);

        if (membership != null && Boolean.TRUE.equals(membership.getActive())) {
            throw new IllegalArgumentException(
                    "User is already an active member");
        }

        if (membership == null) {
            membership = new BusinessMembership();
        }

        membership.setBusinessId(businessId);
        membership.setUserId(request.getUserId());
        membership.setRole(requestedRole);
        membership.setActive(true);

        return mapToResponse(membershipRepository.save(membership));
    }

    @Override
    public List<MembershipResponse> getBusinessMembers(
            Long businessId, Long actorUserId) {

        getActiveBusiness(businessId);
        requireManager(businessId, actorUserId);

        return membershipRepository.findByBusinessIdAndActiveTrue(businessId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<MembershipResponse> getUserMemberships(Long userId) {
        return membershipRepository.findByUserIdAndActiveTrue(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public MembershipResponse getMembership(
            Long businessId, Long targetUserId, Long actorUserId) {

        getActiveBusiness(businessId);

        if (!targetUserId.equals(actorUserId)) {
            requireManager(businessId, actorUserId);
        } else {
            requireActiveMembership(businessId, actorUserId);
        }

        BusinessMembership membership = membershipRepository
                .findByBusinessIdAndUserId(businessId, targetUserId)
                .filter(m -> Boolean.TRUE.equals(m.getActive()))
                .orElseThrow(() ->
                        new IllegalArgumentException("Membership not found"));

        return mapToResponse(membership);
    }

    @Override
    @Transactional
    public void deactivateMember(
            Long businessId, Long targetUserId, Long actorUserId) {

        getActiveBusiness(businessId);
        BusinessRole actorRole = requireManager(businessId, actorUserId);

        BusinessMembership membership = membershipRepository
                .findByBusinessIdAndUserId(businessId, targetUserId)
                .filter(m -> Boolean.TRUE.equals(m.getActive()))
                .orElseThrow(() ->
                        new IllegalArgumentException("Membership not found"));

        if (membership.getRole() == BusinessRole.OWNER) {
            throw new IllegalArgumentException(
                    "Business owner membership cannot be deactivated");
        }

        if (actorRole == BusinessRole.ADMIN
                && membership.getRole() == BusinessRole.ADMIN) {
            throw new AccessDeniedException(
                    "An ADMIN cannot deactivate another ADMIN");
        }

        membership.setActive(false);
        membershipRepository.save(membership);
    }

    private Business getActiveBusiness(Long businessId) {
        return businessRepository.findById(businessId)
                .filter(b -> Boolean.TRUE.equals(b.getActive()))
                .orElseThrow(() ->
                        new IllegalArgumentException("Active business not found"));
    }

    private BusinessRole requireManager(Long businessId, Long userId) {
        BusinessMembership membership = requireActiveMembership(businessId, userId);
        if (membership.getRole() != BusinessRole.OWNER
                && membership.getRole() != BusinessRole.ADMIN) {
            throw new AccessDeniedException(
                    "Only the business owner or an ADMIN can manage members");
        }
        return membership.getRole();
    }

    private BusinessMembership requireActiveMembership(
            Long businessId, Long userId) {

        return membershipRepository.findByBusinessIdAndUserId(businessId, userId)
                .filter(m -> Boolean.TRUE.equals(m.getActive()))
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "You are not an active member of this business"));
    }

    private MembershipResponse mapToResponse(BusinessMembership membership) {
        return new MembershipResponse(
                membership.getId(),
                membership.getBusinessId(),
                membership.getUserId(),
                membership.getRole(),
                membership.getActive(),
                membership.getCreatedAt()
        );
    }
}
