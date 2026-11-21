package com.recoverx.invoice.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recoverx.invoice.dto.InvoiceRequest;
import com.recoverx.invoice.dto.InvoiceResponse;
import com.recoverx.invoice.entity.Invoice;
import com.recoverx.invoice.entity.VerifiedInvoicePayment;
import com.recoverx.invoice.repository.VerifiedInvoicePaymentRepository;
import java.math.BigDecimal;
import com.recoverx.invoice.exception.InvoiceAlreadyExistsException;
import com.recoverx.invoice.exception.InvoiceNotFoundException;
import com.recoverx.invoice.repository.InvoiceRepository;
import com.recoverx.invoice.utility.InvoiceMapper;
import com.recoverx.invoice.client.CustomerConnectionClient;

@Service
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final VerifiedInvoicePaymentRepository verifiedPaymentRepository;
    private final CustomerConnectionClient customerConnectionClient;

    public InvoiceServiceImpl(
            InvoiceRepository invoiceRepository,
            VerifiedInvoicePaymentRepository verifiedPaymentRepository,
            CustomerConnectionClient customerConnectionClient) {

        this.invoiceRepository = invoiceRepository;
        this.verifiedPaymentRepository = verifiedPaymentRepository;
        this.customerConnectionClient = customerConnectionClient;
    }
    
    

    @Override
    public InvoiceResponse createInvoice(Long businessId, InvoiceRequest request) {
        validateBusinessId(businessId);
        validateInvoiceDates(request);

        String invoiceNumber = normalizeInvoiceNumber(request.getInvoiceNumber());

        if (invoiceRepository.existsByBusinessIdAndInvoiceNumberIgnoreCase(
                businessId, invoiceNumber)) {
            throw new InvoiceAlreadyExistsException(
                    "An invoice with this number already exists in this business");
        }

        Invoice invoice = InvoiceMapper.toEntity(businessId, request);
        invoice.setInvoiceNumber(invoiceNumber);
        // Status is initialized by the entity. It is not accepted from the request.

        return InvoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices(Long businessId) {
        validateBusinessId(businessId);

        return invoiceRepository.findAllByBusinessIdOrderByIssueDateDesc(businessId)
                .stream()
                .map(InvoiceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long businessId, Long invoiceId) {
        validateBusinessId(businessId);
        return InvoiceMapper.toResponse(findInvoice(businessId, invoiceId));
    }

    @Override
    public InvoiceResponse updateInvoice(
            Long businessId, Long invoiceId, InvoiceRequest request) {
        validateBusinessId(businessId);
        validateInvoiceDates(request);

        Invoice invoice = findInvoice(businessId, invoiceId);
        String invoiceNumber = normalizeInvoiceNumber(request.getInvoiceNumber());

        if (invoiceRepository.existsByBusinessIdAndInvoiceNumberIgnoreCaseAndIdNot(
                businessId, invoiceNumber, invoiceId)) {
            throw new InvoiceAlreadyExistsException(
                    "Another invoice with this number already exists in this business");
        }

        /*
         * Business ID, customer ID and payment status are intentionally not
         * changed by this general update operation.
         */
        if (request.getAmount().compareTo(invoice.getPaidAmount()) < 0) {
            throw new IllegalArgumentException("Invoice amount cannot be lower than the amount already paid");
        }
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setDescription(request.getDescription());
        invoice.setAmount(request.getAmount());
        updateStatus(invoice);
        invoice.setIssueDate(request.getIssueDate());
        invoice.setDueDate(request.getDueDate());

        return InvoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    @Override
    public InvoiceResponse recordVerifiedPayment(Long businessId, Long invoiceId,
            Long paymentId, BigDecimal amount) {
        validateBusinessId(businessId);
        if (paymentId == null || paymentId <= 0 || amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("A positive payment ID and amount are required");
        }

        // Idempotency: retries for the same Payment Service payment never add the amount twice.
        var existing = verifiedPaymentRepository.findByPaymentId(paymentId);
        if (existing.isPresent()) {
            VerifiedInvoicePayment recorded = existing.get();
            if (!businessId.equals(recorded.getBusinessId())
                    || !invoiceId.equals(recorded.getInvoiceId())
                    || recorded.getAmount().compareTo(amount) != 0) {
                throw new IllegalArgumentException("Payment ID was already recorded with different details");
            }
            return InvoiceMapper.toResponse(findInvoice(businessId, invoiceId));
        }

        Invoice invoice = invoiceRepository.findWithLockByIdAndBusinessId(invoiceId, businessId)
                .orElseThrow(() -> new InvoiceNotFoundException(
                        "Invoice not found with ID: " + invoiceId + " in business ID: " + businessId));
        BigDecimal newPaidAmount = invoice.getPaidAmount().add(amount);
        if (newPaidAmount.compareTo(invoice.getAmount()) > 0) {
            throw new IllegalArgumentException("Verified payment exceeds the invoice outstanding balance");
        }

        VerifiedInvoicePayment recorded = new VerifiedInvoicePayment();
        recorded.setPaymentId(paymentId);
        recorded.setBusinessId(businessId);
        recorded.setInvoiceId(invoiceId);
        recorded.setAmount(amount);
        verifiedPaymentRepository.save(recorded);

        invoice.setPaidAmount(newPaidAmount);
        updateStatus(invoice);
        return InvoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    private void updateStatus(Invoice invoice) {
        int comparison = invoice.getPaidAmount().compareTo(invoice.getAmount());
        if (comparison >= 0) {
            invoice.setStatus("PAID");
        } else if (invoice.getPaidAmount().signum() > 0) {
            invoice.setStatus("PARTIALLY_PAID");
        } else {
            invoice.setStatus("UNPAID");
        }
    }

    private Invoice findInvoice(Long businessId, Long invoiceId) {
        return invoiceRepository.findByIdAndBusinessId(invoiceId, businessId)
                .orElseThrow(() -> new InvoiceNotFoundException(
                        "Invoice not found with ID: " + invoiceId
                                + " in business ID: " + businessId));
    }

    private void validateBusinessId(Long businessId) {
        if (businessId == null || businessId <= 0) {
            throw new IllegalArgumentException("Business ID must be a positive number");
        }
    }

    private void validateInvoiceDates(InvoiceRequest request) {
        if (request.getIssueDate() != null
                && request.getDueDate() != null
                && request.getDueDate().isBefore(request.getIssueDate())) {
            throw new IllegalArgumentException(
                    "Due date cannot be before issue date");
        }
    }

    private String normalizeInvoiceNumber(String invoiceNumber) {
        return invoiceNumber.trim().toUpperCase(Locale.ROOT);
    }
    

@Override
@Transactional(readOnly = true)
public List<InvoiceResponse> getMyInvoices(String authorizationHeader) {

    List<Long> customerIds = getMyCustomerIds(authorizationHeader);

    if (customerIds.isEmpty()) {
        return List.of();
    }

    return invoiceRepository
            .findAllByCustomerIdInOrderByIssueDateDesc(customerIds)
            .stream()
            .map(InvoiceMapper::toResponse)
            .toList();
}

@Override
@Transactional(readOnly = true)
public InvoiceResponse getMyInvoiceById(
        Long invoiceId, String authorizationHeader) {

    if (invoiceId == null || invoiceId <= 0) {
        throw new IllegalArgumentException(
                "Invoice ID must be a positive number");
    }

    List<Long> customerIds = getMyCustomerIds(authorizationHeader);

    if (customerIds.isEmpty()) {
        throw new InvoiceNotFoundException(
                "Invoice not found: " + invoiceId);
    }

    Invoice invoice = invoiceRepository
            .findByIdAndCustomerIdIn(invoiceId, customerIds)
            .orElseThrow(() -> new InvoiceNotFoundException(
                    "Invoice not found: " + invoiceId));

    return InvoiceMapper.toResponse(invoice);
}

private List<Long> getMyCustomerIds(String authorizationHeader) {

    if (authorizationHeader == null || authorizationHeader.isBlank()) {
        throw new IllegalArgumentException(
                "Authorization token is required");
    }

    return customerConnectionClient
            .getMyConnections(authorizationHeader)
            .stream()
            .filter(connection -> connection != null
                    && connection.getId() != null
                    && connection.getUserId() != null)
            .map(connection -> connection.getId())
            .distinct()
            .toList();
}

}
