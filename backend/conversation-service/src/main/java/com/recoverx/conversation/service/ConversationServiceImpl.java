package com.recoverx.conversation.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.recoverx.conversation.dto.*;
import com.recoverx.conversation.client.CustomerClient;
import com.recoverx.conversation.nexconn.service.NexconnService;
import com.recoverx.conversation.entity.Conversation;
import com.recoverx.conversation.exception.*;
import com.recoverx.conversation.repository.ConversationRepository;
import com.recoverx.conversation.utility.ConversationMapper;
import java.util.Map;
import java.util.HashMap;

import com.recoverx.conversation.client.BusinessClient;
import com.recoverx.conversation.dto.BusinessDetails;

@Service
@Transactional
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepository repository;
    private final InvoiceClient invoiceClient;
    private final CustomerClient customerClient;
    private final NexconnService nexconnService;
    private final BusinessClient businessClient;

    public ConversationServiceImpl(
            ConversationRepository repository,
            InvoiceClient invoiceClient,
            CustomerClient customerClient,
            NexconnService nexconnService,
            BusinessClient businessClient) {

        this.repository = repository;
        this.invoiceClient = invoiceClient;
        this.customerClient = customerClient;
        this.nexconnService = nexconnService;
        this.businessClient = businessClient;
    }

    @Override
    public DirectConversationResponse createDirectConversation(Long businessId,
            DirectConversationRequest request, Long ownerUserId, String authorizationHeader) {
        validateId(businessId, "Business ID");
        validateId(ownerUserId, "Owner user ID");
        CustomerDetails customer = customerClient.getCustomer(businessId,
                request.getCustomerId(), authorizationHeader);
        if (!businessId.equals(customer.getBusinessId())
                || !request.getCustomerId().equals(customer.getId())) {
            throw new BusinessRuleException("Customer does not belong to this business");
        }
        if (customer.getUserId() == null) {
            throw new BusinessRuleException("This customer has not accepted the RecoverX invitation yet");
        }
        if (ownerUserId.equals(customer.getUserId())) {
            throw new BusinessRuleException("You cannot start a conversation with yourself");
        }
        if (request.getInvoiceId() != null) {
            InvoiceDetails invoice = invoiceClient.getInvoice(businessId, request.getInvoiceId(), authorizationHeader);
            if (!businessId.equals(invoice.getBusinessId())) {
                throw new BusinessRuleException("Invoice does not belong to this business");
            }
        }
        Conversation conversation = repository
                .findByBusinessIdAndOwnerUserIdAndCustomerUserId(businessId, ownerUserId, customer.getUserId())
                .orElseGet(() -> {
                    Conversation fresh = new Conversation();
                    fresh.setBusinessId(businessId);
                    fresh.setOwnerUserId(ownerUserId);
                    fresh.setCustomerId(customer.getId());
                    fresh.setCustomerUserId(customer.getUserId());
                    fresh.setNexconnChannelId(nexconnService.toNexconnUserId(customer.getUserId()));
                    return repository.save(fresh);
                });
        if (request.getInvoiceId() != null) conversation.setInvoiceId(request.getInvoiceId());
        conversation = repository.save(conversation);
        DirectConversationResponse response = new DirectConversationResponse();
        response.setId(conversation.getId());
        response.setBusinessId(businessId);
        response.setCustomerId(customer.getId());
        response.setOwnerUserId(ownerUserId);
        response.setCustomerUserId(customer.getUserId());
        response.setInvoiceId(conversation.getInvoiceId());
        response.setChannelId(conversation.getNexconnChannelId());
        response.setOtherUserNexconnId(conversation.getNexconnChannelId());
        response.setCurrentUserNexconnId(nexconnService.toNexconnUserId(ownerUserId));
        response.setCreatedAt(conversation.getCreatedAt());
        return response;
    }

    @Override
    public ConversationResponse createConversation(Long businessId, ConversationRequest request, String authorizationHeader) {
        validateId(businessId, "Business ID");
        InvoiceDetails invoice = invoiceClient.getInvoice(businessId, request.getInvoiceId(), authorizationHeader);
        if (!businessId.equals(invoice.getBusinessId())) {
            throw new BusinessRuleException("Invoice does not belong to this business");
        }
        String channelId = request.getNexconnChannelId().trim();
        if (repository.existsByNexconnChannelId(channelId)) {
            throw new ConversationAlreadyExistsException("A conversation already uses this Nexconn channel ID");
        }
        Conversation saved = repository.save(ConversationMapper.toEntity(request, businessId));
        return ConversationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getAllConversations(Long businessId) {
        validateId(businessId, "Business ID");
        return repository.findAllByBusinessIdOrderByCreatedAtDesc(businessId)
            .stream().map(ConversationMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponse getConversationById(Long businessId, Long id) {
        validateId(businessId, "Business ID");
        validateId(id, "Conversation ID");
        Conversation c = repository.findByIdAndBusinessId(id, businessId)
            .orElseThrow(() -> new ConversationNotFoundException(
                "Conversation not found with ID: " + id + " in business: " + businessId));
        return ConversationMapper.toResponse(c);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversationsByInvoice(Long businessId, Long invoiceId) {
        validateId(businessId, "Business ID");
        validateId(invoiceId, "Invoice ID");
        return repository.findAllByBusinessIdAndInvoiceIdOrderByCreatedAtDesc(businessId, invoiceId)
            .stream().map(ConversationMapper::toResponse).toList();
    }

    private void validateId(Long id, String label) {
        if (id == null || id <= 0) throw new BusinessRuleException(label + " must be positive");
    }
    

@Override
@Transactional(readOnly = true)
public List<CustomerConversationResponse> getMyConversations(Long customerUserId) {

    validateId(customerUserId, "Customer user ID");

    List<Conversation> conversations =
            repository.findAllByCustomerUserIdOrderByCreatedAtDesc(customerUserId);

    // Cache business details to avoid repeated API calls
    // when multiple conversations belong to the same business.
    Map<Long, BusinessDetails> businessCache = new HashMap<>();

    return conversations.stream()
            .map(conversation -> {

                CustomerConversationResponse response =
                        new CustomerConversationResponse();

                response.setId(conversation.getId());
                response.setBusinessId(conversation.getBusinessId());
                response.setInvoiceId(conversation.getInvoiceId());
                response.setOwnerUserId(conversation.getOwnerUserId());
                response.setNexconnChannelId(
                        conversation.getNexconnChannelId());

                response.setOwnerNexconnUserId(
                        nexconnService.toNexconnUserId(
                                conversation.getOwnerUserId()));

                response.setCreatedAt(conversation.getCreatedAt());

                // Fetch business details only for businesses
                // associated with this customer's conversations.
                Long businessId = conversation.getBusinessId();

                BusinessDetails businessDetails =
                        businessCache.computeIfAbsent(
                                businessId,
                                businessClient::getCustomerBusinessDetails);

                response.setBusinessName(businessDetails.getName());
                response.setBusinessEmail(businessDetails.getEmail());

                return response;
            })
            .toList();
}

@Override
public DirectConversationResponse createCustomerDirectConversation(
        Long customerUserId,
        Long businessId,
        String authorizationHeader) {

    validateId(customerUserId, "Customer user ID");
    validateId(businessId, "Business ID");

    // 1. Verify that this customer is connected to the requested business.
    List<CustomerDetails> connections =
            customerClient.getMyConnections(authorizationHeader);

    CustomerDetails customer = connections.stream()
            .filter(c -> businessId.equals(c.getBusinessId()))
            .filter(c -> customerUserId.equals(c.getUserId()))
            .findFirst()
            .orElseThrow(() -> new BusinessRuleException(
                    "You are not connected to this business"));

    // 2. Get business details, including its owner user ID.
    BusinessDetails business =
            businessClient.getCustomerBusinessDetails(businessId);

    if (business.getOwnerUserId() == null) {
        throw new BusinessRuleException(
                "Business owner information is unavailable");
    }

    Long ownerUserId = business.getOwnerUserId();

    if (ownerUserId.equals(customerUserId)) {
        throw new BusinessRuleException(
                "You cannot start a conversation with yourself");
    }

    // 3. Find an existing conversation or create a new one.
    Conversation conversation = repository
            .findByBusinessIdAndOwnerUserIdAndCustomerUserId(
                    businessId,
                    ownerUserId,
                    customerUserId)
            .orElseGet(() -> {
                Conversation fresh = new Conversation();

                fresh.setBusinessId(businessId);
                fresh.setOwnerUserId(ownerUserId);
                fresh.setCustomerId(customer.getId());
                fresh.setCustomerUserId(customerUserId);

                // Preserve the existing Nexconn user-ID convention.
                fresh.setNexconnChannelId(
                        nexconnService.toNexconnUserId(customerUserId));

                return repository.save(fresh);
            });

    conversation = repository.save(conversation);

    // 4. Build the response from the customer's perspective.
    DirectConversationResponse response =
            new DirectConversationResponse();

    response.setId(conversation.getId());
    response.setBusinessId(businessId);
    response.setCustomerId(customer.getId());
    response.setOwnerUserId(ownerUserId);
    response.setCustomerUserId(customerUserId);
    response.setInvoiceId(conversation.getInvoiceId());

    response.setChannelId(conversation.getNexconnChannelId());

    // The business owner is the other participant.
    response.setOtherUserNexconnId(
            nexconnService.toNexconnUserId(ownerUserId));

    // The logged-in customer is the current participant.
    response.setCurrentUserNexconnId(
            nexconnService.toNexconnUserId(customerUserId));

    response.setCreatedAt(conversation.getCreatedAt());

    return response;
}
@Override
@Transactional(readOnly = true)
public List<CustomerBusinessContactResponse> getMyBusinessContacts(
        Long customerUserId,
        String authorizationHeader) {

    validateId(customerUserId, "Customer user ID");

    List<CustomerDetails> connections =
            customerClient.getMyConnections(authorizationHeader);

    Map<Long, BusinessDetails> businessCache = new HashMap<>();

    return connections.stream()
            .filter(customer -> customer.getBusinessId() != null)
            .map(customer -> {

                Long businessId = customer.getBusinessId();

                BusinessDetails business =
                        businessCache.computeIfAbsent(
                                businessId,
                                businessClient::getCustomerBusinessDetails
                        );

                return new CustomerBusinessContactResponse(
                        business.getId(),
                        business.getName(),
                        business.getEmail()
                );
            })
            .toList();
}

}
