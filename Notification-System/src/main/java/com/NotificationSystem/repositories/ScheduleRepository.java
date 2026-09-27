package com.NotificationSystem.repositories;

import com.NotificationSystem.entities.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByZone_Id(Long zoneId);

    List<Schedule> findByStatus(Schedule.Status status);

    List<Schedule> findByZone_IdAndStatus(
            Long zoneId,
            Schedule.Status status
    );
}
