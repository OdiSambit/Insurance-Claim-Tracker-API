package com.insurance.claimtracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FileClaimRequest {

    @NotBlank(message = "description is required")
    @Size(max = 500, message = "description must be at most 500 characters")
    private String description;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "1.0", message = "amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "claimDate is required")
    private LocalDate claimDate;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(LocalDate claimDate) {
        this.claimDate = claimDate;
    }
}
