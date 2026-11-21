package com.recoverx.business.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.recoverx.business.dto.MembershipCreateRequest;
import com.recoverx.business.dto.MembershipResponse;
import com.recoverx.business.security.AuthenticatedUser;
import com.recoverx.business.service.MembershipService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/businesses")
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @PostMapping("/{businessId}/members")
    public ResponseEntity<MembershipResponse> addMember(
            @PathVariable Long businessId,
            @Valid @RequestBody MembershipCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        MembershipResponse response = membershipService.addMember(
                businessId, request, currentUser.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{businessId}/members")
    public ResponseEntity<List<MembershipResponse>> getBusinessMembers(
            @PathVariable Long businessId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        return ResponseEntity.ok(
                membershipService.getBusinessMembers(
                        businessId, currentUser.getUserId()));
    }

    // A user can only retrieve their own memberships.
    @GetMapping("/memberships/me")
    public ResponseEntity<List<MembershipResponse>> getMyMemberships(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        return ResponseEntity.ok(
                membershipService.getUserMemberships(currentUser.getUserId()));
    }

    @GetMapping("/{businessId}/members/{userId}")
    public ResponseEntity<MembershipResponse> getMembership(
            @PathVariable Long businessId,
            @PathVariable Long userId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        return ResponseEntity.ok(
                membershipService.getMembership(
                        businessId, userId, currentUser.getUserId()));
    }

    @DeleteMapping("/{businessId}/members/{userId}")
    public ResponseEntity<Void> deactivateMember(
            @PathVariable Long businessId,
            @PathVariable Long userId,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        membershipService.deactivateMember(
                businessId, userId, currentUser.getUserId());

        return ResponseEntity.noContent().build();
    }
}
