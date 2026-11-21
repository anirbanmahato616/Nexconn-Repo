package com.recoverx.commitment.controller;

import com.recoverx.commitment.dto.AICommitmentRequest;
import com.recoverx.commitment.dto.CommitmentResponse;
import com.recoverx.commitment.service.CommitmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Trusted service-to-service endpoint. The shared key must be configured
 * independently in Conversation Service and Commitment Service.
 */
@RestController
@RequestMapping("/api/internal/businesses/{businessId}/commitments")
public class InternalCommitmentController {

    private final CommitmentService commitmentService;
    private final String internalApiKey;

    public InternalCommitmentController(
            CommitmentService commitmentService,
            @Value("${recoverx.internal-api-key:}") String internalApiKey) {
        this.commitmentService = commitmentService;
        this.internalApiKey = internalApiKey;
    }

    @PostMapping("/from-ai-analysis")
    public ResponseEntity<CommitmentResponse> createFromAIAnalysis(
            @PathVariable Long businessId,
            @RequestHeader(name = "X-Internal-Api-Key", required = false) String suppliedKey,
            @Valid @RequestBody AICommitmentRequest request) {

        if (!isValidKey(suppliedKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CommitmentResponse response =
                commitmentService.createFromAIAnalysisInternal(businessId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private boolean isValidKey(String suppliedKey) {
        if (internalApiKey == null || internalApiKey.isBlank()
                || suppliedKey == null || suppliedKey.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                internalApiKey.getBytes(StandardCharsets.UTF_8),
                suppliedKey.getBytes(StandardCharsets.UTF_8));
    }
}
