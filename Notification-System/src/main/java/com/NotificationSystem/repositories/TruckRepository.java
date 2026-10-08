package com.NotificationSystem.repositories;

import com.NotificationSystem.entities.Truck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TruckRepository extends JpaRepository<Truck, Long> {
}