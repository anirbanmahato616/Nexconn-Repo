package com.recoverx.business.service;

import java.util.List;

import com.recoverx.business.dto.MembershipCreateRequest;
import com.recoverx.business.dto.MembershipResponse;

public interface MembershipService {

    MembershipResponse addMember(
            Long businessId, MembershipCreateRequest request, Long actorUserId);

    List<MembershipResponse> getBusinessMembers(Long businessId, Long actorUserId);

    List<MembershipResponse> getUserMemberships(Long userId);

    MembershipResponse getMembership(
            Long businessId, Long targetUserId, Long actorUserId);

    void deactivateMember(Long businessId, Long targetUserId, Long actorUserId);
}
