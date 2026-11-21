package com.recoverx.payment.utility;
import com.recoverx.payment.dto.PaymentRequest;
import com.recoverx.payment.dto.PaymentResponse;
import com.recoverx.payment.entity.Payment;
public final class PaymentMapper {
    private PaymentMapper() {}
    public static Payment toEntity(PaymentRequest request, Long businessId, Long recordedBy) {
        Payment p = new Payment();
        p.setBusinessId(businessId);
        p.setInvoiceId(request.getInvoiceId());
        p.setAmount(request.getAmount());
        p.setPaymentDate(request.getPaymentDate());
        p.setPaymentMethod(request.getPaymentMethod() == null ? null : request.getPaymentMethod().trim());
        p.setTransactionReference(request.getTransactionReference() == null ? null : request.getTransactionReference().trim());
        p.setRecordedBy(recordedBy);
        return p;
    }
    public static PaymentResponse toResponse(Payment p) {
        PaymentResponse r = new PaymentResponse();
        r.setId(p.getId()); r.setBusinessId(p.getBusinessId()); r.setInvoiceId(p.getInvoiceId());
        r.setAmount(p.getAmount()); r.setPaymentDate(p.getPaymentDate()); r.setPaymentMethod(p.getPaymentMethod());
        r.setTransactionReference(p.getTransactionReference()); r.setRecordedBy(p.getRecordedBy());
        r.setVerificationStatus(p.getVerificationStatus()); r.setVerifiedBy(p.getVerifiedBy());
        r.setVerifiedAt(p.getVerifiedAt()); r.setRejectionReason(p.getRejectionReason()); r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}
