package com.recoverx.customer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.recoverx.customer.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findAllByBusinessIdOrderByCreatedAtDesc(Long businessId);

    Optional<Customer> findByIdAndBusinessId(Long id, Long businessId);

    boolean existsByBusinessIdAndEmailIgnoreCase(Long businessId, String email);

    boolean existsByBusinessIdAndEmailIgnoreCaseAndIdNot(
            Long businessId, String email, Long id);
    List<Customer> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
