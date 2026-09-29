package com.NotificationSystem.controller;

import com.NotificationSystem.entities.Notification;
import com.NotificationSystem.entities.Schedule;
import com.NotificationSystem.service.NotificationDispatchService;
import com.NotificationSystem.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private NotificationDispatchService notificationDispatchService;

    @GetMapping
    public ResponseEntity<List<Schedule>> getAllSchedules() {
        return ResponseEntity.ok(scheduleService.getAllSchedules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Schedule> getScheduleById(@PathVariable Long id) {
        Optional<Schedule> schedule =
                scheduleService.getScheduleById(id);

        return schedule.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<List<Schedule>> getSchedulesByZone(
            @PathVariable Long zoneId) {

        return ResponseEntity.ok(
                scheduleService.getSchedulesByZoneId(zoneId)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Schedule>> getSchedulesByStatus(
            @PathVariable Schedule.Status status) {

        return ResponseEntity.ok(
                scheduleService.getSchedulesByStatus(status)
        );
    }

    @PostMapping
    public ResponseEntity<Schedule> createSchedule(
            @RequestBody Schedule schedule) {

        Schedule saved = scheduleService.saveSchedule(schedule);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Schedule> updateSchedule(
            @PathVariable Long id,
            @RequestBody Schedule updatedSchedule) {

        Optional<Schedule> existing =
                scheduleService.getScheduleById(id);

        if (existing.isPresent()) {
            updatedSchedule.setId(id);
            Schedule saved = scheduleService.saveSchedule(updatedSchedule);
            return ResponseEntity.ok(saved);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {

        if (scheduleService.getScheduleById(id).isPresent()) {
            scheduleService.deleteSchedule(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/{id}/notify")
    public ResponseEntity<?> notifyZone(
            @PathVariable Long id,
            @RequestParam("channel") Notification.Channel channel) {

        int queued = notificationDispatchService.queueZoneNotifications(id, channel);
        return ResponseEntity.ok(Map.of("queued", queued));
    }
}


