package com.recoverx.conversation.nexconn.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.recoverx.conversation.nexconn.dto.NexconnTokenResponse;
import com.recoverx.conversation.nexconn.service.NexconnService;
import com.recoverx.conversation.security.AuthenticatedUser;

@RestController
@RequestMapping("/api/nexconn")
public class NexconnTokenController {
    private final NexconnService nexconnService;
    public NexconnTokenController(NexconnService nexconnService) { this.nexconnService = nexconnService; }

    @PostMapping("/token")
    public ResponseEntity<NexconnTokenResponse> issueToken(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(nexconnService.issueToken(currentUser.getUserId(), currentUser.getEmail()));
    }
}
