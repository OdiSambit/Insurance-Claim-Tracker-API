package com.insurance.claimtracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.claimtracker.dto.PolicyResponse;
import com.insurance.claimtracker.exception.ResourceNotFoundException;
import com.insurance.claimtracker.model.PolicyType;
import com.insurance.claimtracker.service.PolicyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PolicyController.class)
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PolicyService policyService;

    private PolicyResponse sampleResponse() {
        PolicyResponse response = new PolicyResponse();
        response.setId(1L);
        response.setType(PolicyType.AUTO);
        response.setHolderName("Asha Rao");
        response.setCoverageAmount(new BigDecimal("100000"));
        response.setStartDate(LocalDate.of(2025, 1, 1));
        response.setEndDate(LocalDate.of(2026, 1, 1));
        response.setPremium(new BigDecimal("3000.00"));
        response.setVehicleNumber("MH12 AB1234");
        response.setDriverAge(30);
        return response;
    }

    @Test
    @DisplayName("POST /policies returns 201 with the premium")
    void createPolicyReturnsCreated() throws Exception {
        when(policyService.createPolicy(any())).thenReturn(sampleResponse());

        Map<String, Object> body = Map.of(
                "type", "AUTO",
                "holderName", "Asha Rao",
                "coverageAmount", 100000,
                "startDate", "2025-01-01",
                "endDate", "2026-01-01",
                "vehicleNumber", "MH12 AB1234",
                "driverAge", 30);

        mockMvc.perform(post("/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.premium").value(3000.00));
    }

    @Test
    @DisplayName("POST /policies with a missing holder name returns 400")
    void createPolicyValidatesInput() throws Exception {
        Map<String, Object> body = Map.of(
                "type", "AUTO",
                "coverageAmount", 100000,
                "startDate", "2025-01-01",
                "endDate", "2026-01-01",
                "vehicleNumber", "MH12 AB1234",
                "driverAge", 30);

        mockMvc.perform(post("/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.holderName").exists());
    }

    @Test
    @DisplayName("GET /policies/{id} returns the policy")
    void getPolicyReturnsOk() throws Exception {
        when(policyService.getPolicy(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/policies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holderName").value("Asha Rao"));
    }

    @Test
    @DisplayName("GET /policies/{id} returns 404 when missing")
    void getPolicyReturnsNotFound() throws Exception {
        when(policyService.getPolicy(99L)).thenThrow(new ResourceNotFoundException("Policy not found with id 99"));

        mockMvc.perform(get("/policies/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
