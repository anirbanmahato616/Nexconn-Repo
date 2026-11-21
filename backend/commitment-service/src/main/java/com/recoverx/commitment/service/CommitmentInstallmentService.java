package com.recoverx.commitment.service;
import java.util.List;
import com.recoverx.commitment.dto.*;
public interface CommitmentInstallmentService {
    CommitmentInstallmentResponse createInstallment(Long businessId, CommitmentInstallmentRequest request);
    List<CommitmentInstallmentResponse> getInstallmentsByCommitmentId(Long businessId, Long commitmentId);
    CommitmentInstallmentResponse getInstallmentById(Long businessId, Long id);
}
