package com.insurance.claimtracker.service.premium;

import com.insurance.claimtracker.model.AutoPolicy;
import com.insurance.claimtracker.model.HomePolicy;
import com.insurance.claimtracker.model.LocationRisk;
import com.insurance.claimtracker.model.Policy;
import com.insurance.claimtracker.model.PolicyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PremiumCalculatorRegistryTest {

    private final PremiumCalculatorRegistry registry =
            new PremiumCalculatorRegistry(List.of(new AutoPremiumCalculator(), new HomePremiumCalculator()));

    @Test
    @DisplayName("routes an auto policy to the auto calculator")
    void routesAutoPolicy() {
        AutoPolicy policy = new AutoPolicy("AB12", 30);
        policy.setCoverageAmount(new BigDecimal("100000"));
        assertThat(registry.calculatePremium(policy)).isEqualByComparingTo("3000.00");
    }

    @Test
    @DisplayName("routes a home policy to the home calculator")
    void routesHomePolicy() {
        HomePolicy policy = new HomePolicy(new BigDecimal("500000"), LocationRisk.LOW);
        policy.setCoverageAmount(new BigDecimal("200000"));
        assertThat(registry.calculatePremium(policy)).isEqualByComparingTo("4500.00");
    }

    @Test
    @DisplayName("fails when no calculator is registered for the policy type")
    void failsForUnregisteredType() {
        PremiumCalculatorRegistry empty = new PremiumCalculatorRegistry(List.of());
        Policy policy = mock(Policy.class);
        when(policy.getType()).thenReturn(PolicyType.AUTO);

        assertThatThrownBy(() -> empty.calculatePremium(policy))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No premium calculator");
    }
}
