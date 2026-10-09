package com.insurance.claimtracker.dto;

import com.insurance.claimtracker.model.LocationRisk;
import com.insurance.claimtracker.model.PolicyType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PolicyResponse {

    private Long id;
    private PolicyType type;
    private String holderName;
    private BigDecimal coverageAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal premium;

    private String vehicleNumber;
    private Integer driverAge;
    private BigDecimal propertyValue;
    private LocationRisk locationRisk;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public BigDecimal getPremium() {
        return premium;
    }

    public void setPremium(BigDecimal premium) {
        this.premium = premium;
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
