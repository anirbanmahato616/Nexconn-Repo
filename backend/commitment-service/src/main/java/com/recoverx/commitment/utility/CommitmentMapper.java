package com.recoverx.commitment.utility;
import com.recoverx.commitment.dto.*;
import com.recoverx.commitment.entity.*;

public final class CommitmentMapper {
    private CommitmentMapper(){}
    public static Commitment toCommitmentEntity(CommitmentRequest r, Long businessId) {
        Commitment c=new Commitment(); c.setBusinessId(businessId); c.setInvoiceId(r.getInvoiceId());
        c.setConversationId(r.getConversationId()); c.setSourceMessageId(r.getSourceMessageId().trim());
        c.setSummary(r.getSummary().trim()); c.setAiClassification(r.getAiClassification());
        c.setProposedAmount(r.getProposedAmount()); c.setProposedPaymentDate(r.getProposedPaymentDate());
        c.setStatus(CommitmentStatus.PENDING_REVIEW); return c;
    }
    public static CommitmentResponse toCommitmentResponse(Commitment c) {
        CommitmentResponse r=new CommitmentResponse(); r.setId(c.getId()); r.setBusinessId(c.getBusinessId());
        r.setInvoiceId(c.getInvoiceId()); r.setConversationId(c.getConversationId());
        r.setSourceMessageId(c.getSourceMessageId()); r.setSummary(c.getSummary());
        r.setStatus(c.getStatus().name()); r.setAiClassification(c.getAiClassification());
        r.setProposedAmount(c.getProposedAmount()); r.setProposedPaymentDate(c.getProposedPaymentDate());
        r.setReviewedBy(c.getReviewedBy()); r.setReviewedAt(c.getReviewedAt());
        r.setReviewNote(c.getReviewNote()); r.setCreatedAt(c.getCreatedAt()); return r;
    }
    public static CommitmentInstallment toInstallmentEntity(CommitmentInstallmentRequest r, Commitment c) {
        CommitmentInstallment i=new CommitmentInstallment(); i.setCommitment(c);
        i.setAmount(r.getAmount()); i.setProposedDate(r.getProposedDate()); i.setStatus("PROPOSED"); return i;
    }
    public static CommitmentInstallmentResponse toInstallmentResponse(CommitmentInstallment i) {
        CommitmentInstallmentResponse r=new CommitmentInstallmentResponse();
        r.setId(i.getId()); r.setCommitmentId(i.getCommitment().getId());
        r.setAmount(i.getAmount()); r.setProposedDate(i.getProposedDate()); r.setStatus(i.getStatus()); return r;
    }
}
