package com.insurance.claimtracker.service.premium;

import com.insurance.claimtracker.model.AutoPolicy;
import com.insurance.claimtracker.model.Policy;
import com.insurance.claimtracker.model.PolicyType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class AutoPremiumCalculator implements PremiumCalculator {

    static final BigDecimal BASE_RATE = new BigDecimal("0.03");
    static final BigDecimal YOUNG_DRIVER_FACTOR = new BigDecimal("1.50");
    static final BigDecimal STANDARD_DRIVER_FACTOR = new BigDecimal("1.00");
    static final BigDecimal SENIOR_DRIVER_FACTOR = new BigDecimal("1.20");

    private static final int YOUNG_DRIVER_MAX_AGE = 24;
    private static final int SENIOR_DRIVER_MIN_AGE = 61;

    @Override
    public PolicyType supportedType() {
        return PolicyType.AUTO;
    }

    @Override
    public BigDecimal calculate(Policy policy) {
        if (!(policy instanceof AutoPolicy auto)) {
            throw new IllegalArgumentException("AutoPremiumCalculator requires an AutoPolicy");
        }
        BigDecimal base = auto.getCoverageAmount().multiply(BASE_RATE);
        return base.multiply(driverFactor(auto.getDriverAge())).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal driverFactor(Integer driverAge) {
        int age = driverAge == null ? 30 : driverAge;
        if (age <= YOUNG_DRIVER_MAX_AGE) {
            return YOUNG_DRIVER_FACTOR;
        }
        if (age >= SENIOR_DRIVER_MIN_AGE) {
            return SENIOR_DRIVER_FACTOR;
        }
        return STANDARD_DRIVER_FACTOR;
    }
}
