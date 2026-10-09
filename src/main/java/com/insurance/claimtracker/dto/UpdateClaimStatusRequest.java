package com.insurance.claimtracker.dto;

import com.insurance.claimtracker.model.ClaimStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateClaimStatusRequest {

    @NotNull(message = "status is required")
    private ClaimStatus status;

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }
}
