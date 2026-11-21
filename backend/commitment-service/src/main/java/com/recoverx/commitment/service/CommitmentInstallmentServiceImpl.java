package com.recoverx.commitment.service;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.recoverx.commitment.dto.*;
import com.recoverx.commitment.entity.*;
import com.recoverx.commitment.exception.*;
import com.recoverx.commitment.repository.*;
import com.recoverx.commitment.utility.CommitmentMapper;

@Service
@Transactional
public class CommitmentInstallmentServiceImpl implements CommitmentInstallmentService {
    private final CommitmentInstallmentRepository installmentRepository;
    private final CommitmentRepository commitmentRepository;
    public CommitmentInstallmentServiceImpl(CommitmentInstallmentRepository ir, CommitmentRepository cr) {
        installmentRepository=ir; commitmentRepository=cr;
    }
    public CommitmentInstallmentResponse createInstallment(Long businessId, CommitmentInstallmentRequest request) {
        Commitment c=commitmentRepository.findByIdAndBusinessId(request.getCommitmentId(),businessId)
            .orElseThrow(()->new CommitmentNotFoundException("Commitment not found in this business"));
        if(c.getStatus()!=CommitmentStatus.APPROVED)
            throw new BusinessRuleException("Installments can be added only to approved commitments");
        if (c.getProposedAmount() != null) {
            java.math.BigDecimal alreadyPlanned = installmentRepository.sumAmountByCommitmentId(c.getId());
            java.math.BigDecimal newTotal = alreadyPlanned.add(request.getAmount());
            if (newTotal.compareTo(c.getProposedAmount()) > 0) {
                throw new BusinessRuleException("Total installment amount cannot exceed the approved commitment amount of " + c.getProposedAmount());
            }
        }
        CommitmentInstallment saved=installmentRepository.save(CommitmentMapper.toInstallmentEntity(request,c));
        return CommitmentMapper.toInstallmentResponse(saved);
    }
    @Transactional(readOnly=true)
    public List<CommitmentInstallmentResponse> getInstallmentsByCommitmentId(Long businessId,Long commitmentId) {
        if(!commitmentRepository.existsByIdAndBusinessId(commitmentId,businessId))
            throw new CommitmentNotFoundException("Commitment not found in this business");
        return installmentRepository.findByCommitment_IdOrderByProposedDateAsc(commitmentId)
            .stream().map(CommitmentMapper::toInstallmentResponse).toList();
    }
    @Transactional(readOnly=true)
    public CommitmentInstallmentResponse getInstallmentById(Long businessId,Long id) {
        CommitmentInstallment i=installmentRepository.findByIdAndCommitment_BusinessId(id,businessId)
            .orElseThrow(()->new CommitmentInstallmentNotFoundException("Installment not found in this business"));
        return CommitmentMapper.toInstallmentResponse(i);
    }
}
