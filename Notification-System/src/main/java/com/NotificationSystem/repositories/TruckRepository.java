package com.NotificationSystem.repositories;

import com.NotificationSystem.entities.Truck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TruckRepository extends JpaRepository<Truck, Long> {


    boolean existsByPlateNumberIgnoreCase(String plateNumber);

    boolean existsByPlateNumberIgnoreCaseAndIdNot(String plateNumber, Long id);


    @Query("select count(s) from Schedule s where s.truck.id = :truckId")
    long countSchedulesUsingTruck(@Param("truckId") Long truckId);
}