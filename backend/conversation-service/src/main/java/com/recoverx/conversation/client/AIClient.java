package com.recoverx.conversation.client;

import com.recoverx.conversation.dto.AIAnalysisRequest;
import com.recoverx.conversation.dto.AIAnalysisResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "ai-service",
    url = "${services.ai.url}")
public interface AIClient {

    @PostMapping("/api/ai/analyze/internal")
    AIAnalysisResponse analyzeMessage(
        @RequestHeader("X-Internal-Api-Key") String apiKey,
        @RequestBody AIAnalysisRequest request
    );
}