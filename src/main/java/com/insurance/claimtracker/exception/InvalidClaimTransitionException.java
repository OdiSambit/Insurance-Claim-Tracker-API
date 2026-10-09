package com.insurance.claimtracker.exception;

import com.insurance.claimtracker.model.ClaimStatus;

public class InvalidClaimTransitionException extends RuntimeException {

    public InvalidClaimTransitionException(ClaimStatus from, ClaimStatus to) {
        super(String.format("Invalid claim status transition from %s to %s", from, to));
    }

    public InvalidClaimTransitionException(String message) {
        super(message);
    }
}
