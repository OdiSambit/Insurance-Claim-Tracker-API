package com.insurance.claimtracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "auto_policies")
@DiscriminatorValue("AUTO")
public class AutoPolicy extends Policy {

    @Column(name = "vehicle_number", nullable = false)
    private String vehicleNumber;

    @Column(name = "driver_age", nullable = false)
    private Integer driverAge;

    public AutoPolicy() {
        // JPA
    }

    public AutoPolicy(String vehicleNumber, Integer driverAge) {
        this.vehicleNumber = vehicleNumber;
        this.driverAge = driverAge;
    }

    @Override
    public PolicyType getType() {
        return PolicyType.AUTO;
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
}
