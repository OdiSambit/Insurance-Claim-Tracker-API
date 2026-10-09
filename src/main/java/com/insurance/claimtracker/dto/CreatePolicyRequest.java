package com.insurance.claimtracker.dto;

import com.insurance.claimtracker.model.LocationRisk;
import com.insurance.claimtracker.model.PolicyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreatePolicyRequest {

    @NotNull(message = "type is required (AUTO or HOME)")
    private PolicyType type;

    @NotBlank(message = "holderName is required")
    @Size(max = 120, message = "holderName must be at most 120 characters")
    private String holderName;

    @NotNull(message = "coverageAmount is required")
    @DecimalMin(value = "1.0", message = "coverageAmount must be greater than zero")
    private BigDecimal coverageAmount;

    @NotNull(message = "startDate is required")
    private LocalDate startDate;

    @NotNull(message = "endDate is required")
    private LocalDate endDate;

    private String vehicleNumber;

    @Min(value = 16, message = "driverAge must be at least 16")
    private Integer driverAge;

    @DecimalMin(value = "1.0", message = "propertyValue must be greater than zero")
    private BigDecimal propertyValue;

    private LocationRisk locationRisk;

    public PolicyType getType() {
        return type;
    }

    public void setType(PolicyType type) {
        this.type = type;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public BigDecimal getCoverageAmount() {
        return coverageAmount;
    }

    public void setCoverageAmount(BigDecimal coverageAmount) {
        this.coverageAmount = coverageAmount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public Integer getDriverAge() {
        return driverAge;
    }

    public void setDriverAge(Integer driverAge) {
        this.driverAge = driverAge;
    }

    public BigDecimal getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(BigDecimal propertyValue) {
        this.propertyValue = propertyValue;
    }

    public LocationRisk getLocationRisk() {
        return locationRisk;
    }

    public void setLocationRisk(LocationRisk locationRisk) {
        this.locationRisk = locationRisk;
    }
}
