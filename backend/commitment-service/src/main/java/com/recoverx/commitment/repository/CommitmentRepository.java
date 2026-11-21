
package com.recoverx.commitment.repository;

import com.recoverx.commitment.entity.Commitment;
import com.recoverx.commitment.entity.CommitmentStatus;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitmentRepository extends JpaRepository<Commitment, Long> {

    List<Commitment> findAllByBusinessIdOrderByCreatedAtDesc(Long businessId);

    Optional<Commitment> findByIdAndBusinessId(Long id, Long businessId);

    boolean existsByIdAndBusinessId(Long id, Long businessId);

    Optional<Commitment> findByBusinessIdAndConversationIdAndSourceMessageId(
            Long businessId, Long conversationId, String sourceMessageId);

    List<Commitment> findAllByInvoiceIdInAndStatusOrderByCreatedAtDesc(
            Collection<Long> invoiceIds, CommitmentStatus status);
}
