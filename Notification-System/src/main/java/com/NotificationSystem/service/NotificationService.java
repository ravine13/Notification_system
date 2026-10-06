package com.NotificationSystem.service;

import com.NotificationSystem.entities.Notification;
import com.NotificationSystem.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // Get all notifications
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    // Get notification by ID
    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    // Create or update notification
    public Notification saveNotification(Notification notification) {
        LocalDateTime now = LocalDateTime.now();
        if (notification.getId() == null) {
            notification.setCreatedAt(now);
            if (notification.getChannel() == null) {
                notification.setChannel(Notification.Channel.SMS);
            }
        }
        notification.setUpdatedAt(now);
        return notificationRepository.save(notification);
    }

    // Delete notification by ID
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    // Get notifications by status
    public List<Notification> getNotificationsByStatus(Notification.Status status) {
        return notificationRepository.findByStatus(status);
    }

    // Get notifications by resident
    public List<Notification> getNotificationsByResidentId(Long residentId) {
        return notificationRepository.findByResident_Id(residentId);
    }

    // Get notifications by schedule
    public List<Notification> getNotificationsByScheduleId(Long scheduleId) {
        return notificationRepository.findBySchedule_Id(scheduleId);
    }
}