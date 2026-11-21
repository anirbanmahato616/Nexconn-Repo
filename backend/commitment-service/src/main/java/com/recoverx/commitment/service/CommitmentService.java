package com.recoverx.commitment.service;
import java.util.List;
import com.recoverx.commitment.dto.*;
import java.util.List;
import com.recoverx.commitment.dto.CustomerCommitmentResponse;

public interface CommitmentService {
    CommitmentResponse createCommitment(Long businessId, CommitmentRequest request);
    CommitmentResponse createFromAIAnalysis(Long businessId, AICommitmentRequest request);
    CommitmentResponse createFromAIAnalysisInternal(Long businessId, AICommitmentRequest request);
    List<CommitmentResponse> getAllCommitments(Long businessId);
    CommitmentResponse getCommitmentById(Long businessId, Long id);
    CommitmentResponse approveCommitment(Long businessId, Long id, CommitmentReviewRequest request, Long reviewerUserId);
    CommitmentResponse rejectCommitment(Long businessId, Long id, CommitmentReviewRequest request, Long reviewerUserId);
    List<CustomerCommitmentResponse> getMyCommitments();
}
