package com.insurance.claimtracker.service.premium;

import com.insurance.claimtracker.model.Policy;
import com.insurance.claimtracker.model.PolicyType;

import java.math.BigDecimal;

/**
 * Strategy for calculating the premium of a policy. One implementation exists per
 * {@link PolicyType}. Implementations are discovered automatically by Spring and
 * selected by {@link PremiumCalculatorRegistry}.
 */
public interface PremiumCalculator {

    PolicyType supportedType();

    BigDecimal calculate(Policy policy);
}
