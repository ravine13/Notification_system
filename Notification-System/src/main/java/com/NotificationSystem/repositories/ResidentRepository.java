package com.NotificationSystem.repositories;

import com.NotificationSystem.entities.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long> {

    Optional<Resident> findByName(String name);

    List<Resident> findByZone_Id(Long zoneId);
}