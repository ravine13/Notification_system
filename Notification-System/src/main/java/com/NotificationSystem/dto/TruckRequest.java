package com.NotificationSystem.dto;

import com.NotificationSystem.entities.Truck;

import java.math.BigDecimal;


public record TruckRequest(
        String plateNumber,
        String model,
        BigDecimal capacityTons,
        String driverName,
        Truck.Status status
) {}