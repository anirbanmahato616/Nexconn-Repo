package com.recoverx.commitment.repository;
import com.recoverx.commitment.entity.CommitmentInstallment;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
public interface CommitmentInstallmentRepository extends JpaRepository<CommitmentInstallment,Long> {
    List<CommitmentInstallment> findByCommitment_IdOrderByProposedDateAsc(Long commitmentId);
    Optional<CommitmentInstallment> findByIdAndCommitment_BusinessId(Long id, Long businessId);
    @Query("select coalesce(sum(i.amount), 0) from CommitmentInstallment i where i.commitment.id = :commitmentId")
    BigDecimal sumAmountByCommitmentId(@Param("commitmentId") Long commitmentId);
}
