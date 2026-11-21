package com.recoverx.commitment.service;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.recoverx.commitment.dto.*;
import com.recoverx.commitment.entity.*;
import com.recoverx.commitment.exception.*;
import com.recoverx.commitment.repository.CommitmentRepository;
import com.recoverx.commitment.utility.CommitmentMapper;

@Service
@Transactional
public class CommitmentServiceImpl implements CommitmentService {
    private final CommitmentRepository repository;
    private final InvoiceClient invoiceClient;
    public CommitmentServiceImpl(CommitmentRepository repository, InvoiceClient invoiceClient) {
        this.repository=repository; this.invoiceClient=invoiceClient;
    }
    public CommitmentResponse createCommitment(Long businessId, CommitmentRequest request) {
        validate(businessId,"Business ID");
        InvoiceDetails invoice=invoiceClient.getInvoice(businessId,request.getInvoiceId());
        if(!businessId.equals(invoice.getBusinessId())) throw new BusinessRuleException("Invoice does not belong to this business");
        if(request.getAiClassification()!=null && !request.getAiClassification().equals("PAYMENT_PROMISE"))
            throw new BusinessRuleException("Only PAYMENT_PROMISE analysis can create a commitment");
        if (request.getConversationId() != null && request.getSourceMessageId() != null) {
            var existing = repository.findByBusinessIdAndConversationIdAndSourceMessageId(
                    businessId, request.getConversationId(), request.getSourceMessageId().trim());
            if (existing.isPresent()) return CommitmentMapper.toCommitmentResponse(existing.get());
        }
        Commitment saved=repository.save(CommitmentMapper.toCommitmentEntity(request,businessId));
        return CommitmentMapper.toCommitmentResponse(saved);
    }

    /**
     * Trusted Conversation Service path. The invoice was already validated when
     * the invoice-specific conversation was created. Webhook requests do not
     * carry an end-user JWT, so InvoiceClient cannot be used from this path.
     * The endpoint is protected by the shared internal API key.
     */
    public CommitmentResponse createFromAIAnalysisInternal(Long businessId, AICommitmentRequest request) {
        validate(businessId, "Business ID");
        if (!"PAYMENT_PROMISE".equalsIgnoreCase(request.getAnalysisType()) || !request.isPaymentIntent()) {
            throw new BusinessRuleException("Only an AI PAYMENT_PROMISE result can create a commitment");
        }
        validate(request.getInvoiceId(), "Invoice ID");
        validate(request.getConversationId(), "Conversation ID");

        String sourceId = request.getSourceMessageId();
        if (sourceId == null || sourceId.isBlank()) {
            throw new BusinessRuleException("Source message ID is required for an internal AI commitment");
        }
        var existing = repository.findByBusinessIdAndConversationIdAndSourceMessageId(
                businessId, request.getConversationId(), sourceId.trim());
        if (existing.isPresent()) {
            return CommitmentMapper.toCommitmentResponse(existing.get());
        }

        CommitmentRequest mapped = new CommitmentRequest();
        mapped.setInvoiceId(request.getInvoiceId());
        mapped.setConversationId(request.getConversationId());
        mapped.setSourceMessageId(sourceId.trim());
        mapped.setSummary(request.getSummary());
        mapped.setAiClassification("PAYMENT_PROMISE");
        mapped.setProposedAmount(request.getAmount());
        mapped.setProposedPaymentDate(request.getProposedDate());
        Commitment saved = repository.save(CommitmentMapper.toCommitmentEntity(mapped, businessId));
        return CommitmentMapper.toCommitmentResponse(saved);
    }

    public CommitmentResponse createFromAIAnalysis(Long businessId, AICommitmentRequest request) {
        validate(businessId, "Business ID");
        if (!"PAYMENT_PROMISE".equalsIgnoreCase(request.getAnalysisType()) || !request.isPaymentIntent()) {
            throw new BusinessRuleException("Only an AI PAYMENT_PROMISE result can create a commitment");
        }
        CommitmentRequest mapped = new CommitmentRequest();
        mapped.setInvoiceId(request.getInvoiceId());
        mapped.setConversationId(request.getConversationId());
        mapped.setSourceMessageId(request.getSourceMessageId() == null || request.getSourceMessageId().isBlank()
                ? "AI-" + request.getInvoiceId() + "-" + request.getProposedDate() + "-" + request.getAmount()
                : request.getSourceMessageId().trim());
        mapped.setSummary(request.getSummary());
        mapped.setAiClassification("PAYMENT_PROMISE");
        mapped.setProposedAmount(request.getAmount());
        mapped.setProposedPaymentDate(request.getProposedDate());
        return createCommitment(businessId, mapped);
    }
    @Transactional(readOnly=true)
    public List<CommitmentResponse> getAllCommitments(Long businessId) {
        validate(businessId,"Business ID");
        return repository.findAllByBusinessIdOrderByCreatedAtDesc(businessId).stream().map(CommitmentMapper::toCommitmentResponse).toList();
    }
    @Transactional(readOnly=true)
    public CommitmentResponse getCommitmentById(Long businessId, Long id) {
        return CommitmentMapper.toCommitmentResponse(find(businessId,id));
    }
    public CommitmentResponse approveCommitment(Long businessId, Long id, CommitmentReviewRequest request, Long reviewerUserId) {
        Commitment c=find(businessId,id);
        if(c.getStatus()!=CommitmentStatus.PENDING_REVIEW) throw new BusinessRuleException("Only pending commitments can be approved");
        c.setStatus(CommitmentStatus.APPROVED); c.setReviewedBy(reviewerUserId);
        c.setReviewedAt(LocalDateTime.now()); c.setReviewNote(request.getNote());
        return CommitmentMapper.toCommitmentResponse(repository.save(c));
    }
    public CommitmentResponse rejectCommitment(Long businessId, Long id, CommitmentReviewRequest request, Long reviewerUserId) {
        Commitment c=find(businessId,id);
        if(c.getStatus()!=CommitmentStatus.PENDING_REVIEW) throw new BusinessRuleException("Only pending commitments can be rejected");
        if(request.getNote()==null || request.getNote().isBlank()) throw new BusinessRuleException("A rejection note is required");
        c.setStatus(CommitmentStatus.REJECTED); c.setReviewedBy(reviewerUserId);
        c.setReviewedAt(LocalDateTime.now()); c.setReviewNote(request.getNote().trim());
        return CommitmentMapper.toCommitmentResponse(repository.save(c));
    }
    private Commitment find(Long businessId,Long id) {
        validate(businessId,"Business ID"); validate(id,"Commitment ID");
        return repository.findByIdAndBusinessId(id,businessId)
            .orElseThrow(()->new CommitmentNotFoundException("Commitment not found with ID: "+id+" in business: "+businessId));
    }
    private void validate(Long id,String label){if(id==null||id<=0)throw new BusinessRuleException(label+" must be positive");}
    
    @Override
    @Transactional(readOnly = true)
    public List<CustomerCommitmentResponse> getMyCommitments() {

        // Get invoices belonging to the authenticated customer.
        List<InvoiceDetails> invoices = invoiceClient.getMyInvoices();

        if (invoices.isEmpty()) {
            return List.of();
        }

        // Extract valid invoice IDs.
        List<Long> invoiceIds = invoices.stream()
                .filter(invoice -> invoice.getId() != null)
                .map(InvoiceDetails::getId)
                .distinct()
                .toList();

        if (invoiceIds.isEmpty()) {
            return List.of();
        }

        // Return only approved commitments for those invoices.
        return repository.findAllByInvoiceIdInAndStatusOrderByCreatedAtDesc(
                        invoiceIds, CommitmentStatus.APPROVED)
                .stream()
                .map(commitment -> new CustomerCommitmentResponse(
                        commitment.getId(),
                        commitment.getBusinessId(),
                        commitment.getInvoiceId(),
                        commitment.getSummary(),
                        commitment.getProposedAmount(),
                        commitment.getProposedPaymentDate(),
                        commitment.getStatus().name(),
                        commitment.getCreatedAt(),
                        commitment.getReviewedAt()
                ))
                .toList();
    }
}
