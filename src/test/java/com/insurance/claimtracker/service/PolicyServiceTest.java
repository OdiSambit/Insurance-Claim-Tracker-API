package com.insurance.claimtracker.service;

import com.insurance.claimtracker.dto.CreatePolicyRequest;
import com.insurance.claimtracker.dto.PolicyResponse;
import com.insurance.claimtracker.exception.BusinessRuleException;
import com.insurance.claimtracker.exception.ResourceNotFoundException;
import com.insurance.claimtracker.model.AutoPolicy;
import com.insurance.claimtracker.model.LocationRisk;
import com.insurance.claimtracker.model.Policy;
import com.insurance.claimtracker.model.PolicyType;
import com.insurance.claimtracker.repository.PolicyRepository;
import com.insurance.claimtracker.service.premium.PremiumCalculatorRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PremiumCalculatorRegistry premiumCalculatorRegistry;

    @InjectMocks
    private PolicyService policyService;

    private CreatePolicyRequest autoRequest() {
        CreatePolicyRequest request = new CreatePolicyRequest();
        request.setType(PolicyType.AUTO);
        request.setHolderName("Asha Rao");
        request.setCoverageAmount(new BigDecimal("100000"));
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setEndDate(LocalDate.of(2026, 1, 1));
        request.setVehicleNumber("MH12 AB1234");
        request.setDriverAge(30);
        return request;
    }

    private CreatePolicyRequest homeRequest() {
        CreatePolicyRequest request = new CreatePolicyRequest();
        request.setType(PolicyType.HOME);
        request.setHolderName("Vikram Singh");
        request.setCoverageAmount(new BigDecimal("200000"));
        request.setStartDate(LocalDate.of(2025, 3, 1));
        request.setEndDate(LocalDate.of(2026, 3, 1));
        request.setPropertyValue(new BigDecimal("500000"));
        request.setLocationRisk(LocationRisk.LOW);
        return request;
    }

    @Test
    @DisplayName("creates an auto policy and returns the calculated premium")
    void createsAutoPolicy() {
        when(premiumCalculatorRegistry.calculatePremium(any(Policy.class))).thenReturn(new BigDecimal("3000.00"));
        when(policyRepository.save(any(Policy.class))).thenAnswer(invocation -> {
            Policy policy = invocation.getArgument(0);
            policy.setId(1L);
            return policy;
        });

        PolicyResponse response = policyService.createPolicy(autoRequest());

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getType()).isEqualTo(PolicyType.AUTO);
        assertThat(response.getPremium()).isEqualByComparingTo("3000.00");
        assertThat(response.getVehicleNumber()).isEqualTo("MH12 AB1234");
        assertThat(response.getDriverAge()).isEqualTo(30);
    }

    @Test
    @DisplayName("creates a home policy and returns the calculated premium")
    void createsHomePolicy() {
        when(premiumCalculatorRegistry.calculatePremium(any(Policy.class))).thenReturn(new BigDecimal("4500.00"));
        when(policyRepository.save(any(Policy.class))).thenAnswer(invocation -> {
            Policy policy = invocation.getArgument(0);
            policy.setId(2L);
            return policy;
        });

        PolicyResponse response = policyService.createPolicy(homeRequest());

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getType()).isEqualTo(PolicyType.HOME);
        assertThat(response.getPremium()).isEqualByComparingTo("4500.00");
        assertThat(response.getLocationRisk()).isEqualTo(LocationRisk.LOW);
    }

    @Test
    @DisplayName("rejects an auto policy without a vehicle number")
    void rejectsAutoWithoutVehicle() {
        CreatePolicyRequest request = autoRequest();
        request.setVehicleNumber(null);

        assertThatThrownBy(() -> policyService.createPolicy(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("vehicleNumber");
        verify(policyRepository, never()).save(any());
    }

    @Test
    @DisplayName("rejects an auto policy without a driver age")
    void rejectsAutoWithoutDriverAge() {
        CreatePolicyRequest request = autoRequest();
        request.setDriverAge(null);

        assertThatThrownBy(() -> policyService.createPolicy(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("driverAge");
    }

    @Test
    @DisplayName("rejects a home policy without a property value")
    void rejectsHomeWithoutPropertyValue() {
        CreatePolicyRequest request = homeRequest();
        request.setPropertyValue(null);

        assertThatThrownBy(() -> policyService.createPolicy(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("propertyValue");
    }

    @Test
    @DisplayName("rejects a home policy without a location risk")
    void rejectsHomeWithoutLocationRisk() {
        CreatePolicyRequest request = homeRequest();
        request.setLocationRisk(null);

        assertThatThrownBy(() -> policyService.createPolicy(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("locationRisk");
    }

    @Test
    @DisplayName("rejects an end date that is not after the start date")
    void rejectsInvalidDateRange() {
        CreatePolicyRequest request = autoRequest();
        request.setEndDate(request.getStartDate());

        assertThatThrownBy(() -> policyService.createPolicy(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("endDate");
    }

    @Test
    @DisplayName("returns a policy by id")
    void returnsPolicyById() {
        AutoPolicy policy = new AutoPolicy("MH12 AB1234", 30);
        policy.setId(7L);
        policy.setHolderName("Asha Rao");
        policy.setCoverageAmount(new BigDecimal("100000"));
        policy.setPremium(new BigDecimal("3000.00"));
        when(policyRepository.findById(7L)).thenReturn(Optional.of(policy));

        PolicyResponse response = policyService.getPolicy(7L);

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getHolderName()).isEqualTo("Asha Rao");
    }

    @Test
    @DisplayName("throws when a policy is not found")
    void throwsWhenPolicyMissing() {
        when(policyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> policyService.getPolicy(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
