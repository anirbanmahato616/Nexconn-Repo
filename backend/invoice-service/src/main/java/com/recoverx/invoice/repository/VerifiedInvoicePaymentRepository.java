package com.recoverx.invoice.repository;

import com.recoverx.invoice.entity.VerifiedInvoicePayment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerifiedInvoicePaymentRepository extends JpaRepository<VerifiedInvoicePayment, Long> {
    Optional<VerifiedInvoicePayment> findByPaymentId(Long paymentId);
}
