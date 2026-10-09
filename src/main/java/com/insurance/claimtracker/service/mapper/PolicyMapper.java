package com.insurance.claimtracker.service.mapper;

import com.insurance.claimtracker.dto.PolicyResponse;
import com.insurance.claimtracker.model.AutoPolicy;
import com.insurance.claimtracker.model.HomePolicy;
import com.insurance.claimtracker.model.Policy;

public final class PolicyMapper {

    private PolicyMapper() {
    }

    public static PolicyResponse toResponse(Policy policy) {
        PolicyResponse response = new PolicyResponse();
        response.setId(policy.getId());
        response.setType(policy.getType());
        response.setHolderName(policy.getHolderName());
        response.setCoverageAmount(policy.getCoverageAmount());
        response.setStartDate(policy.getStartDate());
        response.setEndDate(policy.getEndDate());
        response.setPremium(policy.getPremium());

        if (policy instanceof AutoPolicy auto) {
            response.setVehicleNumber(auto.getVehicleNumber());
            response.setDriverAge(auto.getDriverAge());
        } else if (policy instanceof HomePolicy home) {
            response.setPropertyValue(home.getPropertyValue());
            response.setLocationRisk(home.getLocationRisk());
        }
        return response;
    }
}
