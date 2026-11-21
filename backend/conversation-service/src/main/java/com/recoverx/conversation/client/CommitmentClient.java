package com.recoverx.conversation.client;

import com.recoverx.conversation.dto.AICommitmentRequest;
import com.recoverx.conversation.dto.CommitmentResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "commitment-service",
    url = "${services.commitment.base-url}")
public interface CommitmentClient {

    @PostMapping(
        "/api/internal/businesses/{businessId}/commitments/from-ai-analysis"
    )
    CommitmentResponse createFromAIAnalysis(
        @PathVariable("businessId") Long businessId,

        @RequestHeader("X-Internal-Api-Key") String apiKey,

        @RequestBody AICommitmentRequest request
    );
}