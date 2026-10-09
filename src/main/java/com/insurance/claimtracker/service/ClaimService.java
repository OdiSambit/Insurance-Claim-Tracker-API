package com.insurance.claimtracker.service;

import com.insurance.claimtracker.dto.ClaimResponse;
import com.insurance.claimtracker.dto.FileClaimRequest;
import com.insurance.claimtracker.exception.BusinessRuleException;
import com.insurance.claimtracker.exception.ResourceNotFoundException;
import com.insurance.claimtracker.model.Claim;
import com.insurance.claimtracker.model.ClaimStatus;
import com.insurance.claimtracker.model.Policy;
import com.insurance.claimtracker.repository.ClaimRepository;
import com.insurance.claimtracker.service.claim.ClaimStateMachine;
import com.insurance.claimtracker.service.mapper.ClaimMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final PolicyService policyService;
    private final ClaimStateMachine claimStateMachine;

    public ClaimService(ClaimRepository claimRepository, PolicyService policyService, ClaimStateMachine claimStateMachine) {
        this.claimRepository = claimRepository;
        this.policyService = policyService;
        this.claimStateMachine = claimStateMachine;
    }

    @Transactional
    public ClaimResponse fileClaim(Long policyId, FileClaimRequest request) {
        Policy policy = policyService.loadPolicy(policyId);
        if (request.getAmount().compareTo(policy.getCoverageAmount()) > 0) {
            throw new BusinessRuleException(String.format(
                    "Claim amount %s exceeds policy coverage amount %s",
                    request.getAmount(), policy.getCoverageAmount()));
        }
        Claim claim = new Claim(policy, request.getDescription().trim(), request.getAmount(),
                request.getClaimDate(), ClaimStatus.SUBMITTED);
        Claim saved = claimRepository.save(claim);
        return ClaimMapper.toResponse(saved);
    }

    @Transactional
    public ClaimResponse updateStatus(Long claimId, ClaimStatus target) {
        Claim claim = loadClaim(claimId);
        ClaimStatus next = claimStateMachine.transition(claim.getStatus(), target);
        claim.setStatus(next);
        return ClaimMapper.toResponse(claimRepository.save(claim));
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> listClaims(ClaimStatus status) {
        List<Claim> claims = status == null ? claimRepository.findAll() : claimRepository.findByStatus(status);
        return claims.stream().map(ClaimMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Claim loadClaim(Long id) {
        return claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id " + id));
    }
}
