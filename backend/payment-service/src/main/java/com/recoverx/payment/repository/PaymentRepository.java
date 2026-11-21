package com.recoverx.payment.repository;

import com.recoverx.payment.entity.Payment;
import com.recoverx.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByBusinessIdOrderByCreatedAtDesc(Long businessId);
    Optional<Payment> findByIdAndBusinessId(Long id, Long businessId);
    List<Payment> findAllByBusinessIdAndInvoiceIdOrderByCreatedAtDesc(Long businessId, Long invoiceId);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.businessId = :businessId and p.invoiceId = :invoiceId and p.verificationStatus = :status")
    BigDecimal sumAmountByBusinessAndInvoiceAndStatus(@Param("businessId") Long businessId,
            @Param("invoiceId") Long invoiceId, @Param("status") PaymentStatus status);
    List<Payment> findAllByRecordedByOrderByCreatedAtDesc(Long recordedBy);
}
