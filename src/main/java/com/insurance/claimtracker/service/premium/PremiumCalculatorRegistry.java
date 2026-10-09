package com.insurance.claimtracker.service.premium;

import com.insurance.claimtracker.model.Policy;
import com.insurance.claimtracker.model.PolicyType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the correct {@link PremiumCalculator} for a given policy type.
 * New policy types only require a new {@link PremiumCalculator} bean; no changes here.
 */
@Component
public class PremiumCalculatorRegistry {

    private final Map<PolicyType, PremiumCalculator> calculators = new EnumMap<>(PolicyType.class);

    public PremiumCalculatorRegistry(List<PremiumCalculator> availableCalculators) {
        for (PremiumCalculator calculator : availableCalculators) {
            calculators.put(calculator.supportedType(), calculator);
        }
    }

    public BigDecimal calculatePremium(Policy policy) {
        PremiumCalculator calculator = calculators.get(policy.getType());
        if (calculator == null) {
            throw new IllegalStateException("No premium calculator registered for policy type " + policy.getType());
        }
        return calculator.calculate(policy);
    }
}
