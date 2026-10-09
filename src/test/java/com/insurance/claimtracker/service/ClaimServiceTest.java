package com.insurance.claimtracker.service;

import com.insurance.claimtracker.dto.ClaimResponse;
import com.insurance.claimtracker.dto.FileClaimRequest;
import com.insurance.claimtracker.exception.BusinessRuleException;
import com.insurance.claimtracker.exception.InvalidClaimTransitionException;
import com.insurance.claimtracker.exception.ResourceNotFoundException;
import com.insurance.claimtracker.model.AutoPolicy;
import com.insurance.claimtracker.model.Claim;
import com.insurance.claimtracker.model.ClaimStatus;
import com.insurance.claimtracker.repository.ClaimRepository;
import com.insurance.claimtracker.service.claim.ClaimStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private PolicyService policyService;

    private ClaimService claimService;

    private AutoPolicy policy;

    @BeforeEach
    void setUp() {
        claimService = new ClaimService(claimRepository, policyService, new ClaimStateMachine());
        policy = new AutoPolicy("MH12 AB1234", 30);
        policy.setId(1L);
        policy.setCoverageAmount(new BigDecimal("100000"));
    }

    private FileClaimRequest claimRequest(String amount) {
        FileClaimRequest request = new FileClaimRequest();
        request.setDescription("Bumper damage");
        request.setAmount(new BigDecimal(amount));
        request.setClaimDate(LocalDate.of(2025, 6, 1));
        return request;
    }

    private Claim existingClaim(ClaimStatus status) {
        Claim claim = new Claim(policy, "Bumper damage", new BigDecimal("5000"),
                LocalDate.of(2025, 6, 1), status);
        claim.setId(5L);
        return claim;
    }

    @Test
    @DisplayName("files a claim that starts as SUBMITTED")
    void filesClaim() {
        when(policyService.loadPolicy(1L)).thenReturn(policy);
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> {
            Claim claim = invocation.getArgument(0);
            claim.setId(5L);
            return claim;
        });

        ClaimResponse response = claimService.fileClaim(1L, claimRequest("5000"));

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getPolicyId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(ClaimStatus.SUBMITTED);
    }

    @Test
    @DisplayName("rejects a claim larger than the policy coverage")
    void rejectsClaimAboveCoverage() {
        when(policyService.loadPolicy(1L)).thenReturn(policy);

        assertThatThrownBy(() -> claimService.fileClaim(1L, claimRequest("250000")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("exceeds");
        verify(claimRepository, never()).save(any());
    }

    @Test
    @DisplayName("propagates a missing policy when filing a claim")
    void rejectsClaimForMissingPolicy() {
        when(policyService.loadPolicy(99L)).thenThrow(new ResourceNotFoundException("Policy not found with id 99"));

        assertThatThrownBy(() -> claimService.fileClaim(99L, claimRequest("1000")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("moves a claim from SUBMITTED to UNDER_REVIEW")
    void movesToUnderReview() {
        Claim claim = existingClaim(ClaimStatus.SUBMITTED);
        when(claimRepository.findById(5L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClaimResponse response = claimService.updateStatus(5L, ClaimStatus.UNDER_REVIEW);

        assertThat(response.getStatus()).isEqualTo(ClaimStatus.UNDER_REVIEW);
    }

    @Test
    @DisplayName("rejects an invalid transition with a conflict")
    void rejectsInvalidTransition() {
        Claim claim = existingClaim(ClaimStatus.SUBMITTED);
        when(claimRepository.findById(5L)).thenReturn(Optional.of(claim));

        assertThatThrownBy(() -> claimService.updateStatus(5L, ClaimStatus.APPROVED))
                .isInstanceOf(InvalidClaimTransitionException.class);
        verify(claimRepository, never()).save(any());
    }

    @Test
    @DisplayName("lists all claims when no status filter is given")
    void listsAllClaims() {
        when(claimRepository.findAll()).thenReturn(List.of(existingClaim(ClaimStatus.SUBMITTED)));

        List<ClaimResponse> responses = claimService.listClaims(null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo(ClaimStatus.SUBMITTED);
    }

    @Test
    @DisplayName("lists claims filtered by status")
    void listsClaimsByStatus() {
        when(claimRepository.findByStatus(ClaimStatus.APPROVED))
                .thenReturn(List.of(existingClaim(ClaimStatus.APPROVED)));

        List<ClaimResponse> responses = claimService.listClaims(ClaimStatus.APPROVED);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo(ClaimStatus.APPROVED);
    }
}
