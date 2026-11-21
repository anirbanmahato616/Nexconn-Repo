package com.recoverx.commitment.controller;
import java.util.List;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import com.recoverx.commitment.dto.*;
import com.recoverx.commitment.service.CommitmentInstallmentService;
import com.recoverx.commitment.security.BusinessAccessService;

@RestController
@Validated
@RequestMapping("/api/businesses/{businessId}/commitments")
public class CommitmentInstallmentController {
    private final CommitmentInstallmentService service;
    private final BusinessAccessService accessService;
    public CommitmentInstallmentController(CommitmentInstallmentService service, BusinessAccessService accessService){
        this.service=service; this.accessService=accessService;
    }
    @PostMapping("/installments")
    public ResponseEntity<CommitmentInstallmentResponse> create(@PathVariable @Positive Long businessId,@Valid @RequestBody CommitmentInstallmentRequest request){
        accessService.requireOwnerOrAdmin(businessId);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createInstallment(businessId,request));
    }
    @GetMapping("/{commitmentId}/installments")
    public ResponseEntity<List<CommitmentInstallmentResponse>> getByCommitment(@PathVariable @Positive Long businessId,@PathVariable @Positive Long commitmentId){
        accessService.requireActiveMember(businessId);
        return ResponseEntity.ok(service.getInstallmentsByCommitmentId(businessId,commitmentId));
    }
    @GetMapping("/installments/{id}")
    public ResponseEntity<CommitmentInstallmentResponse> getById(@PathVariable @Positive Long businessId,@PathVariable @Positive Long id){
        accessService.requireActiveMember(businessId);
        return ResponseEntity.ok(service.getInstallmentById(businessId,id));
    }
}
