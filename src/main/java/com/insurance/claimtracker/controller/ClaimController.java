package com.insurance.claimtracker.controller;

import com.insurance.claimtracker.dto.ClaimResponse;
import com.insurance.claimtracker.dto.FileClaimRequest;
import com.insurance.claimtracker.dto.UpdateClaimStatusRequest;
import com.insurance.claimtracker.model.ClaimStatus;
import com.insurance.claimtracker.service.ClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Claims", description = "File claims and move them through the review lifecycle")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping("/policies/{policyId}/claims")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "File a claim against a policy", description = "The claim starts in SUBMITTED status.")
    public ClaimResponse fileClaim(@PathVariable Long policyId, @Valid @RequestBody FileClaimRequest request) {
        return claimService.fileClaim(policyId, request);
    }

    @PatchMapping("/claims/{id}/status")
    @Operation(summary = "Change a claim's status",
            description = "Allowed: SUBMITTED -> UNDER_REVIEW -> APPROVED or REJECTED. Invalid transitions return 409.")
    public ClaimResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateClaimStatusRequest request) {
        return claimService.updateStatus(id, request.getStatus());
    }

    @GetMapping("/claims")
    @Operation(summary = "List claims, optionally filtered by status")
    public List<ClaimResponse> listClaims(@RequestParam(required = false) ClaimStatus status) {
        return claimService.listClaims(status);
    }
}
