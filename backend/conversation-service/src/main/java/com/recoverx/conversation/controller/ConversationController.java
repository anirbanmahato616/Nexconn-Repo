package com.recoverx.conversation.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import com.recoverx.conversation.dto.*;
import com.recoverx.conversation.service.ConversationService;
import com.recoverx.conversation.client.BusinessMembershipClient;
import com.recoverx.conversation.security.AuthenticatedUser;

@RestController
@Validated
@RequestMapping("/api/businesses/{businessId}/conversations")
public class ConversationController {
    private final ConversationService conversationService;
    private final BusinessMembershipClient businessMembershipClient;

    public ConversationController(ConversationService conversationService,
            BusinessMembershipClient businessMembershipClient) {
        this.conversationService = conversationService;
        this.businessMembershipClient = businessMembershipClient;
    }

    @PostMapping("/direct")
    public ResponseEntity<DirectConversationResponse> createDirectConversation(
            @PathVariable @Positive Long businessId,
            @Valid @RequestBody DirectConversationRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        authorize(businessId, currentUser, authorization);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                conversationService.createDirectConversation(businessId, request,
                        currentUser.getUserId(), authorization));
    }

    @PostMapping
    public ResponseEntity<ConversationResponse> createConversation(
            @PathVariable @Positive Long businessId,
            @Valid @RequestBody ConversationRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        authorize(businessId, currentUser, authorization);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(conversationService.createConversation(businessId, request, authorization));
    }

    @GetMapping
    public ResponseEntity<List<ConversationResponse>> getAllConversations(
            @PathVariable @Positive Long businessId,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        authorize(businessId, currentUser, authorization);
        return ResponseEntity.ok(conversationService.getAllConversations(businessId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversationResponse> getConversationById(
            @PathVariable @Positive Long businessId, @PathVariable @Positive Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        authorize(businessId, currentUser, authorization);
        return ResponseEntity.ok(conversationService.getConversationById(businessId, id));
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<List<ConversationResponse>> getConversationsByInvoice(
            @PathVariable @Positive Long businessId, @PathVariable @Positive Long invoiceId,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        authorize(businessId, currentUser, authorization);
        return ResponseEntity.ok(conversationService.getConversationsByInvoice(businessId, invoiceId));
    }

    private void authorize(Long businessId, AuthenticatedUser currentUser,
            String authorization) {
        businessMembershipClient.requireActiveMembership(
                businessId, currentUser.getUserId(), authorization);
    }
}
