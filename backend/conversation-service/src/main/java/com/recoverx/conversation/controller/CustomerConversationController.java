package com.recoverx.conversation.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.recoverx.conversation.dto.CustomerConversationResponse;
import com.recoverx.conversation.dto.CustomerDirectConversationRequest;
import com.recoverx.conversation.dto.DirectConversationResponse;
import com.recoverx.conversation.security.AuthenticatedUser;
import com.recoverx.conversation.service.ConversationService;
import com.recoverx.conversation.dto.CustomerBusinessContactResponse;

@RestController
@RequestMapping("/api/customers/me/conversations")
public class CustomerConversationController {

    private final ConversationService conversationService;

    public CustomerConversationController(
            ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public ResponseEntity<List<CustomerConversationResponse>> getMyConversations(
            @AuthenticationPrincipal AuthenticatedUser user) {

        return ResponseEntity.ok(
                conversationService.getMyConversations(user.getUserId()));
    }

    @PostMapping("/direct")
    public ResponseEntity<DirectConversationResponse> createDirectConversation(
            @Valid @RequestBody CustomerDirectConversationRequest request,
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {

        return ResponseEntity.ok(
                conversationService.createCustomerDirectConversation(
                        user.getUserId(),
                        request.getBusinessId(),
                        authorizationHeader));
    }
    @GetMapping("/businesses")
    public ResponseEntity<List<CustomerBusinessContactResponse>> getMyBusinessContacts(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {

        return ResponseEntity.ok(
                conversationService.getMyBusinessContacts(
                        user.getUserId(),
                        authorizationHeader
                )
        );
    }
}