package com.recoverx.customer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recoverx.customer.entity.CustomerInvitation;
import com.recoverx.customer.entity.InvitationStatus;

public interface CustomerInvitationRepository extends JpaRepository<CustomerInvitation, Long> {
    Optional<CustomerInvitation> findByTokenHash(String tokenHash);
    List<CustomerInvitation> findAllByBusinessIdAndCustomerIdAndStatus(
            Long businessId, Long customerId, InvitationStatus status);
}
