package com.recoverx.ai.controller;

import com.recoverx.ai.client.BusinessMembershipClient;
import com.recoverx.ai.dto.AIAnalysisRequest;
import com.recoverx.ai.dto.PaymentAnalysisResponse;
import com.recoverx.ai.security.AuthenticatedUser;
import com.recoverx.ai.service.GeminiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIAnalysisController {
    private final GeminiService geminiService;
    private final BusinessMembershipClient membershipClient;
    private final String internalApiKey;

    public AIAnalysisController(GeminiService geminiService,
                                BusinessMembershipClient membershipClient,
                                @Value("${recoverx.internal-api-key:}") String internalApiKey) {
        this.geminiService = geminiService;
        this.membershipClient = membershipClient;
        this.internalApiKey = internalApiKey;
    }

    @PostMapping("/analyze")
    public ResponseEntity<PaymentAnalysisResponse> analyzeMessage(
            @Valid @RequestBody AIAnalysisRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {

        membershipClient.requireActiveMembership(
                request.getBusinessId(), authenticatedUser, authorizationHeader);

        return ResponseEntity.ok(geminiService.analyzePaymentMessage(request));
    }

    /**
     * Server-to-server endpoint used only by Conversation Service webhook processing.
     * It deliberately does not trust a user JWT; access is controlled by a shared secret.
     */
    @PostMapping("/analyze/internal")
    public ResponseEntity<PaymentAnalysisResponse> analyzeInternal(
            @Valid @RequestBody AIAnalysisRequest request,
            @RequestHeader(name = "X-Internal-Api-Key", required = false) String suppliedKey) {
        if (internalApiKey == null || internalApiKey.isBlank()
                || suppliedKey == null
                || !MessageDigest.isEqual(
                        internalApiKey.getBytes(StandardCharsets.UTF_8),
                        suppliedKey.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid internal service credentials");
        }
        return ResponseEntity.ok(geminiService.analyzePaymentMessage(request));
    }
}
