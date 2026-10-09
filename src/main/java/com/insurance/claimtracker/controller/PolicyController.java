package com.insurance.claimtracker.controller;

import com.insurance.claimtracker.dto.CreatePolicyRequest;
import com.insurance.claimtracker.dto.PolicyResponse;
import com.insurance.claimtracker.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/policies")
@Tag(name = "Policies", description = "Create and retrieve insurance policies")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a policy", description = "Creates an auto or home policy and returns its calculated premium.")
    public PolicyResponse createPolicy(@Valid @RequestBody CreatePolicyRequest request) {
        return policyService.createPolicy(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Fetch a policy by id")
    public ResponseEntity<PolicyResponse> getPolicy(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.getPolicy(id));
    }
}
