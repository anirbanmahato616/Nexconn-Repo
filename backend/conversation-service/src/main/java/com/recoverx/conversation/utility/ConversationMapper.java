package com.recoverx.conversation.utility;
import com.recoverx.conversation.dto.*;
import com.recoverx.conversation.entity.Conversation;

public final class ConversationMapper {
    private ConversationMapper() {}
    public static Conversation toEntity(ConversationRequest request, Long businessId) {
        Conversation c = new Conversation();
        c.setBusinessId(businessId);
        c.setInvoiceId(request.getInvoiceId());
        c.setNexconnChannelId(request.getNexconnChannelId().trim());
        return c;
    }
    public static ConversationResponse toResponse(Conversation c) {
        ConversationResponse r = new ConversationResponse();
        r.setId(c.getId()); r.setBusinessId(c.getBusinessId()); r.setInvoiceId(c.getInvoiceId());
        r.setOwnerUserId(c.getOwnerUserId()); r.setCustomerId(c.getCustomerId()); r.setCustomerUserId(c.getCustomerUserId());
        r.setNexconnChannelId(c.getNexconnChannelId()); r.setCreatedAt(c.getCreatedAt());
        return r;
    }
}
