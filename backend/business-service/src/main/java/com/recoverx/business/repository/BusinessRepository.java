package com.recoverx.business.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.recoverx.business.entity.Business;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {

    List<Business> findByOwnerUserId(Long ownerUserId);

    List<Business> findByOwnerUserIdAndActiveTrue(Long ownerUserId);

    boolean existsByIdAndOwnerUserId(Long id, Long ownerUserId);
}