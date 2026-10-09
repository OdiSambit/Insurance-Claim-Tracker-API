package com.insurance.claimtracker.service.premium;

import com.insurance.claimtracker.model.HomePolicy;
import com.insurance.claimtracker.model.LocationRisk;
import com.insurance.claimtracker.model.Policy;
import com.insurance.claimtracker.model.PolicyType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class HomePremiumCalculator implements PremiumCalculator {

    static final BigDecimal COVERAGE_RATE = new BigDecimal("0.02");
    static final BigDecimal PROPERTY_VALUE_RATE = new BigDecimal("0.001");
    static final BigDecimal LOW_RISK_FACTOR = new BigDecimal("1.00");
    static final BigDecimal MEDIUM_RISK_FACTOR = new BigDecimal("1.20");
    static final BigDecimal HIGH_RISK_FACTOR = new BigDecimal("1.50");

    @Override
    public PolicyType supportedType() {
        return PolicyType.HOME;
    }

    @Override
    public BigDecimal calculate(Policy policy) {
        if (!(policy instanceof HomePolicy home)) {
            throw new IllegalArgumentException("HomePremiumCalculator requires a HomePolicy");
        }
        BigDecimal coverageComponent = home.getCoverageAmount().multiply(COVERAGE_RATE);
        BigDecimal propertyComponent = home.getPropertyValue().multiply(PROPERTY_VALUE_RATE);
        BigDecimal base = coverageComponent.add(propertyComponent);
        return base.multiply(riskFactor(home.getLocationRisk())).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal riskFactor(LocationRisk risk) {
        if (risk == null) {
            return LOW_RISK_FACTOR;
        }
        return switch (risk) {
            case LOW -> LOW_RISK_FACTOR;
            case MEDIUM -> MEDIUM_RISK_FACTOR;
            case HIGH -> HIGH_RISK_FACTOR;
        };
    }
}
