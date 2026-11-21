package com.recoverx.conversation.service;
import java.util.List;
import com.recoverx.conversation.dto.*;
import com.recoverx.conversation.dto.CustomerConversationResponse;

public interface ConversationService {
    DirectConversationResponse createDirectConversation(Long businessId, DirectConversationRequest request, Long ownerUserId, String authorizationHeader);
    ConversationResponse createConversation(Long businessId, ConversationRequest request, String authorizationHeader);
    List<ConversationResponse> getAllConversations(Long businessId);
    ConversationResponse getConversationById(Long businessId, Long id);
    List<ConversationResponse> getConversationsByInvoice(Long businessId, Long invoiceId);
    List<CustomerConversationResponse> getMyConversations(Long customerUserId);
    DirectConversationResponse createCustomerDirectConversation(
            Long customerUserId,
            Long businessId,
            String authorizationHeader
    );
    List<CustomerBusinessContactResponse> getMyBusinessContacts(
            Long customerUserId,
            String authorizationHeader
    );
}
