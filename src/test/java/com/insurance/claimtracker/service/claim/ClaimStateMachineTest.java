package com.insurance.claimtracker.service.claim;

import com.insurance.claimtracker.exception.InvalidClaimTransitionException;
import com.insurance.claimtracker.model.ClaimStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimStateMachineTest {

    private final ClaimStateMachine stateMachine = new ClaimStateMachine();

    @Test
    @DisplayName("SUBMITTED can move to UNDER_REVIEW")
    void submittedToUnderReview() {
        assertThat(stateMachine.transition(ClaimStatus.SUBMITTED, ClaimStatus.UNDER_REVIEW))
                .isEqualTo(ClaimStatus.UNDER_REVIEW);
    }

    @Test
    @DisplayName("UNDER_REVIEW can move to APPROVED")
    void underReviewToApproved() {
        assertThat(stateMachine.transition(ClaimStatus.UNDER_REVIEW, ClaimStatus.APPROVED))
                .isEqualTo(ClaimStatus.APPROVED);
    }

    @Test
    @DisplayName("UNDER_REVIEW can move to REJECTED")
    void underReviewToRejected() {
        assertThat(stateMachine.transition(ClaimStatus.UNDER_REVIEW, ClaimStatus.REJECTED))
                .isEqualTo(ClaimStatus.REJECTED);
    }

    @Test
    @DisplayName("SUBMITTED cannot jump to APPROVED")
    void submittedCannotJumpToApproved() {
        assertThatThrownBy(() -> stateMachine.transition(ClaimStatus.SUBMITTED, ClaimStatus.APPROVED))
                .isInstanceOf(InvalidClaimTransitionException.class)
                .hasMessageContaining("SUBMITTED")
                .hasMessageContaining("APPROVED");
    }

    @Test
    @DisplayName("SUBMITTED cannot jump to REJECTED")
    void submittedCannotJumpToRejected() {
        assertThatThrownBy(() -> stateMachine.transition(ClaimStatus.SUBMITTED, ClaimStatus.REJECTED))
                .isInstanceOf(InvalidClaimTransitionException.class);
    }

    @ParameterizedTest
    @EnumSource(ClaimStatus.class)
    @DisplayName("a terminal claim cannot transition anywhere")
    void terminalStatesRejectAllTransitions(ClaimStatus target) {
        assertThatThrownBy(() -> stateMachine.transition(ClaimStatus.APPROVED, target))
                .isInstanceOf(InvalidClaimTransitionException.class);
        assertThatThrownBy(() -> stateMachine.transition(ClaimStatus.REJECTED, target))
                .isInstanceOf(InvalidClaimTransitionException.class);
    }

    @Test
    @DisplayName("a claim cannot transition to the same state")
    void cannotTransitionToSameState() {
        assertThatThrownBy(() -> stateMachine.transition(ClaimStatus.UNDER_REVIEW, ClaimStatus.UNDER_REVIEW))
                .isInstanceOf(InvalidClaimTransitionException.class);
    }

    @Test
    @DisplayName("null current or target status is rejected")
    void nullStatusesRejected() {
        assertThatThrownBy(() -> stateMachine.transition(null, ClaimStatus.UNDER_REVIEW))
                .isInstanceOf(InvalidClaimTransitionException.class);
        assertThatThrownBy(() -> stateMachine.transition(ClaimStatus.SUBMITTED, null))
                .isInstanceOf(InvalidClaimTransitionException.class);
    }

    @Test
    @DisplayName("canTransition reflects the allowed transitions")
    void canTransitionHelper() {
        assertThat(stateMachine.canTransition(ClaimStatus.SUBMITTED, ClaimStatus.UNDER_REVIEW)).isTrue();
        assertThat(stateMachine.canTransition(ClaimStatus.SUBMITTED, ClaimStatus.APPROVED)).isFalse();
        assertThat(stateMachine.canTransition(null, ClaimStatus.APPROVED)).isFalse();
    }

    @Test
    @DisplayName("isTerminal identifies final states")
    void isTerminalHelper() {
        assertThat(stateMachine.isTerminal(ClaimStatus.APPROVED)).isTrue();
        assertThat(stateMachine.isTerminal(ClaimStatus.REJECTED)).isTrue();
        assertThat(stateMachine.isTerminal(ClaimStatus.SUBMITTED)).isFalse();
        assertThat(stateMachine.isTerminal(null)).isFalse();
    }
}
