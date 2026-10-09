package com.insurance.claimtracker.service.premium;

import com.insurance.claimtracker.model.AutoPolicy;
import com.insurance.claimtracker.model.HomePolicy;
import com.insurance.claimtracker.model.LocationRisk;
import com.insurance.claimtracker.model.PolicyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HomePremiumCalculatorTest {

    private final HomePremiumCalculator calculator = new HomePremiumCalculator();

    private HomePolicy policy(BigDecimal coverage, BigDecimal propertyValue, LocationRisk risk) {
        HomePolicy policy = new HomePolicy(propertyValue, risk);
        policy.setCoverageAmount(coverage);
        return policy;
    }

    @Test
    @DisplayName("supports the HOME policy type")
    void supportsHomeType() {
        assertThat(calculator.supportedType()).isEqualTo(PolicyType.HOME);
    }

    @Test
    @DisplayName("low risk home uses the base rate")
    void lowRiskPremium() {
        BigDecimal premium = calculator.calculate(
                policy(new BigDecimal("200000"), new BigDecimal("500000"), LocationRisk.LOW));
        assertThat(premium).isEqualByComparingTo("4500.00");
    }

    @Test
    @DisplayName("medium risk home applies a 1.2x loading")
    void mediumRiskPremium() {
        BigDecimal premium = calculator.calculate(
                policy(new BigDecimal("200000"), new BigDecimal("500000"), LocationRisk.MEDIUM));
        assertThat(premium).isEqualByComparingTo("5400.00");
    }

    @Test
    @DisplayName("high risk home applies a 1.5x loading")
    void highRiskPremium() {
        BigDecimal premium = calculator.calculate(
                policy(new BigDecimal("200000"), new BigDecimal("500000"), LocationRisk.HIGH));
        assertThat(premium).isEqualByComparingTo("6750.00");
    }

    @Test
    @DisplayName("null risk defaults to the low risk factor")
    void nullRiskDefaultsToLow() {
        BigDecimal premium = calculator.calculate(
                policy(new BigDecimal("200000"), new BigDecimal("500000"), null));
        assertThat(premium).isEqualByComparingTo("4500.00");
    }

    @Test
    @DisplayName("rejects a non home policy")
    void rejectsWrongPolicyType() {
        assertThatThrownBy(() -> calculator.calculate(new AutoPolicy("X", 30)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
