package com.insurance.claimtracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "home_policies")
@DiscriminatorValue("HOME")
public class HomePolicy extends Policy {

    @Column(name = "property_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal propertyValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_risk", nullable = false)
    private LocationRisk locationRisk;

    public HomePolicy() {
        // JPA
    }

    public HomePolicy(BigDecimal propertyValue, LocationRisk locationRisk) {
        this.propertyValue = propertyValue;
        this.locationRisk = locationRisk;
    }

    @Override
    public PolicyType getType() {
        return PolicyType.HOME;
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
