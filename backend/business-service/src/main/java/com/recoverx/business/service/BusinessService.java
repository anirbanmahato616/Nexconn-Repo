package com.recoverx.business.service;

import java.util.List;

import com.recoverx.business.dto.BusinessCreateRequest;
import com.recoverx.business.dto.BusinessUpdateRequest;
import com.recoverx.business.dto.BusinessResponse;
import com.recoverx.business.dto.CustomerBusinessDetailsResponse;

public interface BusinessService {

    BusinessResponse createBusiness(BusinessCreateRequest request, Long ownerUserId);

    List<BusinessResponse> getBusinessesByOwner(Long ownerUserId);

    BusinessResponse getBusinessById(Long businessId, Long actorUserId);

    BusinessResponse updateBusiness(
            Long businessId, BusinessUpdateRequest request, Long actorUserId);

    void deactivateBusiness(Long businessId, Long actorUserId);
    CustomerBusinessDetailsResponse getCustomerBusinessDetails(Long businessId);
}
