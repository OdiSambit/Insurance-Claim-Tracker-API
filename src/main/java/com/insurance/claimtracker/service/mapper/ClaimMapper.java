package com.insurance.claimtracker.service.mapper;

import com.insurance.claimtracker.dto.ClaimResponse;
import com.insurance.claimtracker.model.Claim;

public final class ClaimMapper {

    private ClaimMapper() {
    }

    public static ClaimResponse toResponse(Claim claim) {
        ClaimResponse response = new ClaimResponse();
        response.setId(claim.getId());
        response.setPolicyId(claim.getPolicy() == null ? null : claim.getPolicy().getId());
        response.setDescription(claim.getDescription());
        response.setAmount(claim.getAmount());
        response.setStatus(claim.getStatus());
        response.setClaimDate(claim.getClaimDate());
        return response;
    }
}
