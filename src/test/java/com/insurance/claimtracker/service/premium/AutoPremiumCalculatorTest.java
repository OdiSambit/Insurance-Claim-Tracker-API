package com.insurance.claimtracker.service.premium;

import com.insurance.claimtracker.model.AutoPolicy;
import com.insurance.claimtracker.model.PolicyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AutoPremiumCalculatorTest {

    private final AutoPremiumCalculator calculator = new AutoPremiumCalculator();

    private AutoPolicy policy(BigDecimal coverage, int driverAge) {
        AutoPolicy policy = new AutoPolicy("AB12 CDE", driverAge);
        policy.setCoverageAmount(coverage);
        return policy;
    }

    @Test
    @DisplayName("supports the AUTO policy type")
    void supportsAutoType() {
        assertThat(calculator.supportedType()).isEqualTo(PolicyType.AUTO);
    }

    @Test
    @DisplayName("standard driver applies the 3% base rate")
    void standardDriverPremium() {
        BigDecimal premium = calculator.calculate(policy(new BigDecimal("100000"), 30));
        assertThat(premium).isEqualByComparingTo("3000.00");
    }

    @Test
    @DisplayName("young driver pays a 1.5x loading")
    void youngDriverPremium() {
        BigDecimal premium = calculator.calculate(policy(new BigDecimal("100000"), 20));
        assertThat(premium).isEqualByComparingTo("4500.00");
    }

    @Test
    @DisplayName("senior driver pays a 1.2x loading")
    void seniorDriverPremium() {
        BigDecimal premium = calculator.calculate(policy(new BigDecimal("100000"), 70));
        assertThat(premium).isEqualByComparingTo("3600.00");
    }

    @Test
    @DisplayName("age boundaries 25 and 60 use the standard factor")
    void boundaryAgesUseStandardFactor() {
        assertThat(calculator.calculate(policy(new BigDecimal("100000"), 25))).isEqualByComparingTo("3000.00");
        assertThat(calculator.calculate(policy(new BigDecimal("100000"), 60))).isEqualByComparingTo("3000.00");
    }

    @Test
    @DisplayName("premium is rounded to two decimals")
    void premiumIsRounded() {
        BigDecimal premium = calculator.calculate(policy(new BigDecimal("12345"), 20));
        assertThat(premium).isEqualByComparingTo("555.53");
    }

    @Test
    @DisplayName("rejects a non auto policy")
    void rejectsWrongPolicyType() {
        assertThatThrownBy(() -> calculator.calculate(new com.insurance.claimtracker.model.HomePolicy()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
