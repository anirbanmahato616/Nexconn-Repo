package com.recoverx.business.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recoverx.business.dto.BusinessCreateRequest;
import com.recoverx.business.dto.BusinessResponse;
import com.recoverx.business.dto.BusinessUpdateRequest;
import com.recoverx.business.entity.Business;
import com.recoverx.business.entity.BusinessMembership;
import com.recoverx.business.entity.BusinessRole;
import com.recoverx.business.repository.BusinessMembershipRepository;
import com.recoverx.business.repository.BusinessRepository;
import com.recoverx.business.dto.CustomerBusinessDetailsResponse;

@Service
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessMembershipRepository membershipRepository;

    public BusinessServiceImpl(
            BusinessRepository businessRepository,
            BusinessMembershipRepository membershipRepository) {
        this.businessRepository = businessRepository;
        this.membershipRepository = membershipRepository;
    }

    @Override
    @Transactional
    public BusinessResponse createBusiness(
            BusinessCreateRequest request, Long ownerUserId) {

        Business business = new Business();
        business.setName(request.getName().trim());
        business.setOwnerUserId(ownerUserId);
        business.setEmail(request.getEmail());
        business.setPhone(request.getPhone());
        business.setAddress(request.getAddress());
        business.setIndustry(request.getIndustry());
        business.setActive(true);

        Business savedBusiness = businessRepository.save(business);

        BusinessMembership membership = new BusinessMembership();
        membership.setBusinessId(savedBusiness.getId());
        membership.setUserId(ownerUserId);
        membership.setRole(BusinessRole.OWNER);
        membership.setActive(true);
        membershipRepository.save(membership);

        return mapToResponse(savedBusiness);
    }

    @Override
    public List<BusinessResponse> getBusinessesByOwner(Long ownerUserId) {
        return businessRepository.findByOwnerUserIdAndActiveTrue(ownerUserId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public BusinessResponse getBusinessById(Long businessId, Long actorUserId) {
        Business business = getActiveBusiness(businessId);
        requireActiveMembership(businessId, actorUserId);
        return mapToResponse(business);
    }

    @Override
    @Transactional
    public BusinessResponse updateBusiness(
            Long businessId, BusinessUpdateRequest request, Long actorUserId) {

        Business business = getActiveBusiness(businessId);
        requireRole(businessId, actorUserId, BusinessRole.OWNER);

        business.setName(request.getName().trim());
        business.setEmail(request.getEmail());
        business.setPhone(request.getPhone());
        business.setAddress(request.getAddress());
        business.setIndustry(request.getIndustry());

        return mapToResponse(businessRepository.save(business));
    }

    @Override
    @Transactional
    public void deactivateBusiness(Long businessId, Long actorUserId) {
        Business business = getActiveBusiness(businessId);
        requireRole(businessId, actorUserId, BusinessRole.OWNER);
        business.setActive(false);
        businessRepository.save(business);
    }

    private Business getActiveBusiness(Long businessId) {
        return businessRepository.findById(businessId)
                .filter(b -> Boolean.TRUE.equals(b.getActive()))
                .orElseThrow(() ->
                        new IllegalArgumentException("Active business not found"));
    }

    private void requireActiveMembership(Long businessId, Long userId) {
        BusinessMembership membership = membershipRepository
                .findByBusinessIdAndUserId(businessId, userId)
                .filter(m -> Boolean.TRUE.equals(m.getActive()))
                .orElseThrow(() ->
                        new AccessDeniedException("You are not an active member of this business"));
    }

    private void requireRole(Long businessId, Long userId, BusinessRole requiredRole) {
        BusinessMembership membership = membershipRepository
                .findByBusinessIdAndUserId(businessId, userId)
                .filter(m -> Boolean.TRUE.equals(m.getActive()))
                .orElseThrow(() ->
                        new AccessDeniedException("You are not an active member of this business"));

        if (membership.getRole() != requiredRole) {
            throw new AccessDeniedException(
                    "Only the business owner can perform this operation");
        }
    }

    private BusinessResponse mapToResponse(Business business) {
        return new BusinessResponse(
                business.getId(),
                business.getName(),
                business.getOwnerUserId(),
                business.getEmail(),
                business.getPhone(),
                business.getAddress(),
                business.getIndustry(),
                business.getActive(),
                business.getCreatedAt(),
                business.getUpdatedAt()
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public CustomerBusinessDetailsResponse getCustomerBusinessDetails(Long businessId) {

        Business business = getActiveBusiness(businessId);

        return new CustomerBusinessDetailsResponse(
                business.getId(),
                business.getName(),
                business.getEmail(),
                business.getOwnerUserId()
        );
    }
}
