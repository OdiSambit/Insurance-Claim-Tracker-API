package com.insurance.claimtracker.service.claim;

import com.insurance.claimtracker.exception.InvalidClaimTransitionException;
import com.insurance.claimtracker.model.ClaimStatus;
import org.springframework.stereotype.Component;

/**
 * Enforces the claim lifecycle:
 * SUBMITTED -> UNDER_REVIEW -> APPROVED | REJECTED.
 * APPROVED and REJECTED are terminal; no transition may leave them.
 */
@Component
public class ClaimStateMachine {

    public ClaimStatus transition(ClaimStatus current, ClaimStatus target) {
        if (current == null) {
            throw new InvalidClaimTransitionException("Current claim status must not be null");
        }
        if (target == null) {
            throw new InvalidClaimTransitionException("Target claim status must not be null");
        }
        if (!current.allowedNextStates().contains(target)) {
            throw new InvalidClaimTransitionException(current, target);
        }
        return target;
    }

    public boolean canTransition(ClaimStatus current, ClaimStatus target) {
        return current != null && target != null && current.allowedNextStates().contains(target);
    }

    public boolean isTerminal(ClaimStatus status) {
        return status != null && status.isFinal();
    }
}
