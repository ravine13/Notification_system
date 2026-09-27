package com.NotificationSystem.repositories;

import com.NotificationSystem.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findBySchedule_Id(Long scheduleId);

    List<Notification> findByResident_Id(Long residentId);

    List<Notification> findByStatus(Notification.Status status);

    List<Notification> findBySchedule_IdAndResident_Id(
            Long scheduleId,
            Long residentId
    );
}