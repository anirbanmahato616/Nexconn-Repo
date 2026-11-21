package com.recoverx.commitment.controller;
import java.util.List;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import com.recoverx.commitment.dto.*;
import com.recoverx.commitment.service.CommitmentService;
import com.recoverx.commitment.security.BusinessAccessService;
import com.recoverx.commitment.security.AuthenticatedUser;

@RestController
@Validated
@RequestMapping("/api/businesses/{businessId}/commitments")
public class CommitmentController {
    private final CommitmentService service;
    private final BusinessAccessService accessService;
    public CommitmentController(CommitmentService service, BusinessAccessService accessService){
        this.service=service; this.accessService=accessService;
    }
    @PostMapping
    public ResponseEntity<CommitmentResponse> create(@PathVariable @Positive Long businessId,@Valid @RequestBody CommitmentRequest request){
        accessService.requireActiveMember(businessId);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createCommitment(businessId,request));
    }

    /** Creates a pending-review commitment from the structured AI analysis response. */
    @PostMapping("/from-ai-analysis")
    public ResponseEntity<CommitmentResponse> createFromAIAnalysis(
            @PathVariable @Positive Long businessId,
            @Valid @RequestBody AICommitmentRequest request) {
        accessService.requireActiveMember(businessId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createFromAIAnalysis(businessId, request));
    }
    @GetMapping
    public ResponseEntity<List<CommitmentResponse>> getAll(@PathVariable @Positive Long businessId){
        accessService.requireActiveMember(businessId);
        return ResponseEntity.ok(service.getAllCommitments(businessId));
    }
    @GetMapping("/{id}")
    public ResponseEntity<CommitmentResponse> getById(@PathVariable @Positive Long businessId,@PathVariable @Positive Long id){
        accessService.requireActiveMember(businessId);
        return ResponseEntity.ok(service.getCommitmentById(businessId,id));
    }
    @PatchMapping("/{id}/approve")
    public ResponseEntity<CommitmentResponse> approve(@PathVariable @Positive Long businessId,@PathVariable @Positive Long id,@Valid @RequestBody CommitmentReviewRequest request){
        AuthenticatedUser reviewer=accessService.requireOwnerOrAdmin(businessId);
        return ResponseEntity.ok(service.approveCommitment(businessId,id,request,reviewer.getUserId()));
    }
    @PatchMapping("/{id}/reject")
    public ResponseEntity<CommitmentResponse> reject(@PathVariable @Positive Long businessId,@PathVariable @Positive Long id,@Valid @RequestBody CommitmentReviewRequest request){
        AuthenticatedUser reviewer=accessService.requireOwnerOrAdmin(businessId);
        return ResponseEntity.ok(service.rejectCommitment(businessId,id,request,reviewer.getUserId()));
    }
}
