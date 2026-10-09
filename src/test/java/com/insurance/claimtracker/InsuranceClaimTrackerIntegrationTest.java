package com.insurance.claimtracker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InsuranceClaimTrackerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long createPolicy() throws Exception {
        Map<String, Object> body = Map.of(
                "type", "AUTO",
                "holderName", "Integration User",
                "coverageAmount", 100000,
                "startDate", "2025-01-01",
                "endDate", "2026-01-01",
                "vehicleNumber", "KA01 XY9999",
                "driverAge", 32);

        String json = mockMvc.perform(post("/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(json);
        return node.get("id").asLong();
    }

    private long fileClaim(long policyId, double amount) throws Exception {
        Map<String, Object> body = Map.of(
                "description", "Windshield crack",
                "amount", amount,
                "claimDate", "2025-06-15");

        String json = mockMvc.perform(post("/policies/" + policyId + "/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(json).get("id").asLong();
    }

    @Test
    @DisplayName("full policy and claim lifecycle works end to end")
    void fullLifecycle() throws Exception {
        long policyId = createPolicy();

        mockMvc.perform(get("/policies/" + policyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.premium").isNumber());

        long claimId = fileClaim(policyId, 40000);

        mockMvc.perform(patch("/claims/" + claimId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "UNDER_REVIEW"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"));

        mockMvc.perform(patch("/claims/" + claimId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "APPROVED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(patch("/claims/" + claimId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "REJECTED"))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("invalid claim jump returns 409")
    void invalidJump() throws Exception {
        long policyId = createPolicy();
        long claimId = fileClaim(policyId, 10000);

        mockMvc.perform(patch("/claims/" + claimId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "APPROVED"))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("claim above coverage is rejected with 400")
    void claimAboveCoverage() throws Exception {
        long policyId = createPolicy();

        Map<String, Object> body = Map.of(
                "description", "Total loss",
                "amount", 500000,
                "claimDate", "2025-06-15");

        mockMvc.perform(post("/policies/" + policyId + "/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("missing policy returns 404")
    void missingPolicy() throws Exception {
        mockMvc.perform(get("/policies/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("listing claims filtered by status works")
    void listByStatus() throws Exception {
        long policyId = createPolicy();
        fileClaim(policyId, 10000);

        mockMvc.perform(get("/claims").param("status", "SUBMITTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
