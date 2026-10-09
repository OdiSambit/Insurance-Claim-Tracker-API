package com.insurance.claimtracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.claimtracker.dto.ClaimResponse;
import com.insurance.claimtracker.exception.BusinessRuleException;
import com.insurance.claimtracker.exception.InvalidClaimTransitionException;
import com.insurance.claimtracker.exception.ResourceNotFoundException;
import com.insurance.claimtracker.model.ClaimStatus;
import com.insurance.claimtracker.service.ClaimService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClaimController.class)
class ClaimControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClaimService claimService;

    private ClaimResponse sampleResponse(ClaimStatus status) {
        ClaimResponse response = new ClaimResponse();
        response.setId(5L);
        response.setPolicyId(1L);
        response.setDescription("Bumper damage");
        response.setAmount(new BigDecimal("5000"));
        response.setClaimDate(LocalDate.of(2025, 6, 1));
        response.setStatus(status);
        return response;
    }

    @Test
    @DisplayName("POST /policies/{id}/claims returns 201")
    void fileClaimReturnsCreated() throws Exception {
        when(claimService.fileClaim(eq(1L), any())).thenReturn(sampleResponse(ClaimStatus.SUBMITTED));

        Map<String, Object> body = Map.of(
                "description", "Bumper damage",
                "amount", 5000,
                "claimDate", "2025-06-01");

        mockMvc.perform(post("/policies/1/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    @DisplayName("POST /policies/{id}/claims returns 404 for a missing policy")
    void fileClaimPolicyNotFound() throws Exception {
        when(claimService.fileClaim(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Policy not found with id 99"));

        Map<String, Object> body = Map.of(
                "description", "Bumper damage",
                "amount", 5000,
                "claimDate", "2025-06-01");

        mockMvc.perform(post("/policies/99/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /policies/{id}/claims returns 400 when amount exceeds coverage")
    void fileClaimAboveCoverage() throws Exception {
        when(claimService.fileClaim(eq(1L), any()))
                .thenThrow(new BusinessRuleException("Claim amount exceeds policy coverage amount"));

        Map<String, Object> body = Map.of(
                "description", "Bumper damage",
                "amount", 500000,
                "claimDate", "2025-06-01");

        mockMvc.perform(post("/policies/1/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /claims/{id}/status returns the updated claim")
    void updateStatusReturnsOk() throws Exception {
        when(claimService.updateStatus(5L, ClaimStatus.UNDER_REVIEW))
                .thenReturn(sampleResponse(ClaimStatus.UNDER_REVIEW));

        mockMvc.perform(patch("/claims/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "UNDER_REVIEW"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"));
    }

    @Test
    @DisplayName("PATCH /claims/{id}/status returns 409 for an invalid transition")
    void updateStatusConflict() throws Exception {
        when(claimService.updateStatus(5L, ClaimStatus.APPROVED))
                .thenThrow(new InvalidClaimTransitionException(ClaimStatus.SUBMITTED, ClaimStatus.APPROVED));

        mockMvc.perform(patch("/claims/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "APPROVED"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("GET /claims filters by status")
    void listClaimsByStatus() throws Exception {
        when(claimService.listClaims(ClaimStatus.SUBMITTED))
                .thenReturn(List.of(sampleResponse(ClaimStatus.SUBMITTED)));

        mockMvc.perform(get("/claims").param("status", "SUBMITTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SUBMITTED"));
    }

    @Test
    @DisplayName("GET /claims without a filter returns all claims")
    void listAllClaims() throws Exception {
        when(claimService.listClaims(null)).thenReturn(List.of(sampleResponse(ClaimStatus.SUBMITTED)));

        mockMvc.perform(get("/claims"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /claims with an invalid status returns 400")
    void listClaimsInvalidStatus() throws Exception {
        mockMvc.perform(get("/claims").param("status", "NOT_A_STATUS"))
                .andExpect(status().isBadRequest());
    }
}
