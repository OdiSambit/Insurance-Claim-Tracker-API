package com.insurance.claimtracker.model;

import java.util.EnumSet;
import java.util.Set;

public enum ClaimStatus {
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED;

    public boolean isFinal() {
        return this == APPROVED || this == REJECTED;
    }

    public Set<ClaimStatus> allowedNextStates() {
        return switch (this) {
            case SUBMITTED -> EnumSet.of(UNDER_REVIEW);
            case UNDER_REVIEW -> EnumSet.of(APPROVED, REJECTED);
            case APPROVED, REJECTED -> EnumSet.noneOf(ClaimStatus.class);
        };
    }
}
