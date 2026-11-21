
package com.recoverx.invoice.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

import com.recoverx.invoice.entity.Invoice;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findAllByBusinessIdOrderByIssueDateDesc(Long businessId);

    Optional<Invoice> findByIdAndBusinessId(Long id, Long businessId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Invoice> findWithLockByIdAndBusinessId(Long id, Long businessId);

    boolean existsByBusinessIdAndInvoiceNumberIgnoreCase(
            Long businessId, String invoiceNumber);

    boolean existsByBusinessIdAndInvoiceNumberIgnoreCaseAndIdNot(
            Long businessId, String invoiceNumber, Long id);

    // Customer Mode queries
    List<Invoice> findAllByCustomerIdInOrderByIssueDateDesc(
            Collection<Long> customerIds);

    Optional<Invoice> findByIdAndCustomerIdIn(
            Long invoiceId, Collection<Long> customerIds);
}
