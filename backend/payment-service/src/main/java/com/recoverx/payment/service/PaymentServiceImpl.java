package com.recoverx.payment.service;

import com.recoverx.payment.dto.*;
import com.recoverx.payment.entity.Payment;
import com.recoverx.payment.entity.PaymentStatus;
import com.recoverx.payment.exception.*;
import com.recoverx.payment.repository.PaymentRepository;
import com.recoverx.payment.utility.PaymentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository repository;
    private final InvoiceClient invoiceClient;
    private final CustomerClient customerClient;

    public PaymentServiceImpl(PaymentRepository repository, InvoiceClient invoiceClient,
            CustomerClient customerClient) {
        this.repository = repository;
        this.invoiceClient = invoiceClient;
        this.customerClient = customerClient;
    }

    @Override
    public PaymentResponse createPayment(Long businessId, PaymentRequest request, Long actorUserId) {
        validateBusinessId(businessId);
        InvoiceDetails invoice = invoiceClient.getInvoice(businessId, request.getInvoiceId());
        if (!businessId.equals(invoice.getBusinessId())) {
            throw new BusinessRuleException("Invoice does not belong to this business");
        }
        customerClient.requireCustomerOwnsRecord(businessId, invoice.getCustomerId(), actorUserId);
        BigDecimal alreadyVerified = repository.sumAmountByBusinessAndInvoiceAndStatus(
            businessId, request.getInvoiceId(), PaymentStatus.VERIFIED);
        BigDecimal outstanding = invoice.getAmount().subtract(alreadyVerified);
        if (outstanding.signum() <= 0) {
            throw new BusinessRuleException("This invoice has no outstanding balance");
        }
        if (request.getAmount().compareTo(outstanding) > 0) {
            throw new BusinessRuleException("Payment amount exceeds outstanding balance: " + outstanding);
        }
        Payment payment = PaymentMapper.toEntity(request, businessId, actorUserId);
        payment.setVerificationStatus(PaymentStatus.PENDING);
        return PaymentMapper.toResponse(repository.save(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments(Long businessId) {
        validateBusinessId(businessId);
        return repository.findAllByBusinessIdOrderByCreatedAtDesc(businessId)
            .stream().map(PaymentMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long businessId, Long id) {
        return PaymentMapper.toResponse(findPayment(businessId, id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByInvoice(Long businessId, Long invoiceId) {
        validateBusinessId(businessId);
        return repository.findAllByBusinessIdAndInvoiceIdOrderByCreatedAtDesc(businessId, invoiceId)
            .stream().map(PaymentMapper::toResponse).toList();
    }

    @Override
    public PaymentResponse verifyPayment(Long businessId, Long id, PaymentReviewRequest request, Long reviewerUserId) {
        Payment p = findPayment(businessId, id);
        if (p.getVerificationStatus() != PaymentStatus.PENDING) {
            throw new BusinessRuleException("Only pending payments can be verified");
        }
        // Update Invoice Service first. Its paymentId-based idempotency makes retries safe
        // if the following Payment Service database save fails.
        invoiceClient.recordVerifiedPayment(businessId, p.getInvoiceId(), p.getId(), p.getAmount());
        p.setVerificationStatus(PaymentStatus.VERIFIED);
        p.setVerifiedBy(reviewerUserId);
        p.setVerifiedAt(LocalDateTime.now());
        p.setRejectionReason(null);
        return PaymentMapper.toResponse(repository.save(p));
    }

    @Override
    public PaymentResponse rejectPayment(Long businessId, Long id, PaymentReviewRequest request, Long reviewerUserId) {
        Payment p = findPayment(businessId, id);
        if (p.getVerificationStatus() != PaymentStatus.PENDING) {
            throw new BusinessRuleException("Only pending payments can be rejected");
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new BusinessRuleException("A rejection reason is required");
        }
        p.setVerificationStatus(PaymentStatus.REJECTED);
        p.setVerifiedBy(reviewerUserId);
        p.setVerifiedAt(LocalDateTime.now());
        p.setRejectionReason(request.getReason().trim());
        return PaymentMapper.toResponse(repository.save(p));
    }

    private Payment findPayment(Long businessId, Long id) {
        validateBusinessId(businessId);
        return repository.findByIdAndBusinessId(id, businessId)
            .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + id
                + " in business: " + businessId));
    }
    private void validateBusinessId(Long businessId) {
        if (businessId == null || businessId <= 0) throw new BusinessRuleException("Business ID must be positive");
    }
    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getMyPayments(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessRuleException("Invalid user ID");
        }

        return repository.findAllByRecordedByOrderByCreatedAtDesc(userId)
                .stream()
                .map(PaymentMapper::toResponse)
                .toList();
    }
}
