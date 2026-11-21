
package com.recoverx.business.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.recoverx.business.entity.BusinessMembership;

@Repository
public interface BusinessMembershipRepository
        extends JpaRepository<BusinessMembership, Long> {

    List<BusinessMembership> findByUserIdAndActiveTrue(Long userId);

    List<BusinessMembership> findByBusinessIdAndActiveTrue(Long businessId);

    Optional<BusinessMembership> findByBusinessIdAndUserId(
            Long businessId, Long userId);

    boolean existsByBusinessIdAndUserIdAndActiveTrue(
            Long businessId, Long userId);
}
