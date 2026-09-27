package com.NotificationSystem.controller;

import com.NotificationSystem.entities.Notification;
import com.NotificationSystem.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(
            @PathVariable Long id) {

        Optional<Notification> notification =
                notificationService.getNotificationById(id);

        return notification.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<List<Notification>> getNotificationsBySchedule(
            @PathVariable Long scheduleId) {

        return ResponseEntity.ok(
                notificationService.getNotificationsByScheduleId(scheduleId)
        );
    }

    @GetMapping("/resident/{residentId}")
    public ResponseEntity<List<Notification>> getNotificationsByResident(
            @PathVariable Long residentId) {

        return ResponseEntity.ok(
                notificationService.getNotificationsByResidentId(residentId)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Notification>> getNotificationsByStatus(
            @PathVariable Notification.Status status) {

        return ResponseEntity.ok(
                notificationService.getNotificationsByStatus(status)
        );
    }

    @PostMapping
    public ResponseEntity<Notification> createNotification(
            @RequestBody Notification notification) {

        Notification saved =
                notificationService.saveNotification(notification);

        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Notification> updateNotification(
            @PathVariable Long id,
            @RequestBody Notification updatedNotification) {

        Optional<Notification> existing =
                notificationService.getNotificationById(id);

        if (existing.isPresent()) {
            updatedNotification.setId(id);
            Notification saved =
                    notificationService.saveNotification(updatedNotification);

            return ResponseEntity.ok(saved);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {

        if (notificationService.getNotificationById(id).isPresent()) {
            notificationService.deleteNotification(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}