package com.insurance.claimtracker.service;

import com.insurance.claimtracker.dto.CreatePolicyRequest;
import com.insurance.claimtracker.dto.PolicyResponse;
import com.insurance.claimtracker.exception.BusinessRuleException;
import com.insurance.claimtracker.exception.ResourceNotFoundException;
import com.insurance.claimtracker.model.AutoPolicy;
import com.insurance.claimtracker.model.HomePolicy;
import com.insurance.claimtracker.model.Policy;
import com.insurance.claimtracker.repository.PolicyRepository;
import com.insurance.claimtracker.service.mapper.PolicyMapper;
import com.insurance.claimtracker.service.premium.PremiumCalculatorRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final PremiumCalculatorRegistry premiumCalculatorRegistry;

    public PolicyService(PolicyRepository policyRepository, PremiumCalculatorRegistry premiumCalculatorRegistry) {
        this.policyRepository = policyRepository;
        this.premiumCalculatorRegistry = premiumCalculatorRegistry;
    }

    @Transactional
    public PolicyResponse createPolicy(CreatePolicyRequest request) {
        validateDates(request);
        Policy policy = buildPolicy(request);
        policy.setPremium(premiumCalculatorRegistry.calculatePremium(policy));
        Policy saved = policyRepository.save(policy);
        return PolicyMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PolicyResponse getPolicy(Long id) {
        return PolicyMapper.toResponse(loadPolicy(id));
    }

    @Transactional(readOnly = true)
    public Policy loadPolicy(Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id " + id));
    }

    private Policy buildPolicy(CreatePolicyRequest request) {
        return switch (request.getType()) {
            case AUTO -> buildAutoPolicy(request);
            case HOME -> buildHomePolicy(request);
        };
    }

    private AutoPolicy buildAutoPolicy(CreatePolicyRequest request) {
        if (!StringUtils.hasText(request.getVehicleNumber())) {
            throw new BusinessRuleException("vehicleNumber is required for auto policies");
        }
        if (request.getDriverAge() == null) {
            throw new BusinessRuleException("driverAge is required for auto policies");
        }
        AutoPolicy policy = new AutoPolicy(request.getVehicleNumber().trim(), request.getDriverAge());
        applyCommon(policy, request);
        return policy;
    }

    private HomePolicy buildHomePolicy(CreatePolicyRequest request) {
        if (request.getPropertyValue() == null) {
            throw new BusinessRuleException("propertyValue is required for home policies");
        }
        if (request.getLocationRisk() == null) {
            throw new BusinessRuleException("locationRisk is required for home policies");
        }
        HomePolicy policy = new HomePolicy(request.getPropertyValue(), request.getLocationRisk());
        applyCommon(policy, request);
        return policy;
    }

    private void applyCommon(Policy policy, CreatePolicyRequest request) {
        policy.setHolderName(request.getHolderName().trim());
        policy.setCoverageAmount(request.getCoverageAmount());
        policy.setStartDate(request.getStartDate());
        policy.setEndDate(request.getEndDate());
    }

    private void validateDates(CreatePolicyRequest request) {
        BigDecimal coverage = request.getCoverageAmount();
        if (coverage != null && coverage.signum() <= 0) {
            throw new BusinessRuleException("coverageAmount must be greater than zero");
        }
        if (request.getStartDate() != null && request.getEndDate() != null
                && !request.getEndDate().isAfter(request.getStartDate())) {
            throw new BusinessRuleException("endDate must be after startDate");
        }
    }
}
