
package com.recoverx.commitment.controller;

import com.recoverx.commitment.dto.CustomerCommitmentResponse;
import com.recoverx.commitment.service.CommitmentService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers/me/commitments")
public class CustomerCommitmentController {

    private final CommitmentService commitmentService;

    public CustomerCommitmentController(CommitmentService commitmentService) {
        this.commitmentService = commitmentService;
    }

    @GetMapping
    public ResponseEntity<List<CustomerCommitmentResponse>> getMyCommitments() {
        return ResponseEntity.ok(commitmentService.getMyCommitments());
    }
}
