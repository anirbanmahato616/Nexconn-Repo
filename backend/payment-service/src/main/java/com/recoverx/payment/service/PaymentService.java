package com.recoverx.payment.service;

import com.recoverx.payment.dto.*;
import java.util.List;

public interface PaymentService {
    PaymentResponse createPayment(Long businessId, PaymentRequest request, Long actorUserId);
    List<PaymentResponse> getAllPayments(Long businessId);
    PaymentResponse getPaymentById(Long businessId, Long id);
    List<PaymentResponse> getPaymentsByInvoice(Long businessId, Long invoiceId);
    PaymentResponse verifyPayment(Long businessId, Long id, PaymentReviewRequest request, Long reviewerUserId);
    PaymentResponse rejectPayment(Long businessId, Long id, PaymentReviewRequest request, Long reviewerUserId);
    List<PaymentResponse> getMyPayments(Long userId);
}
