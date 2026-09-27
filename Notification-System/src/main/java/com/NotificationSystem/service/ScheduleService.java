package com.NotificationSystem.service;

import com.NotificationSystem.entities.Schedule;
import com.NotificationSystem.repositories.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    // Get all schedules
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }
    // Get schedule by ID
    public Optional<Schedule> getScheduleById(Long id) {
        return scheduleRepository.findById(id);
    }
    // Create or update schedule
    public Schedule saveSchedule(Schedule schedule) {
        LocalDateTime now = LocalDateTime.now();
        if(schedule.getId() == null) {
            schedule.setCreatedAt(now);
        }
        schedule.setUpdatedAt(now);
        return scheduleRepository.save(schedule);
    }
    // Delete schedule by ID
    public void deleteSchedule(Long id) {
        scheduleRepository.deleteById(id);
    }
    public List<Schedule> getScheduleByStatus(Schedule.Status status) {
        return scheduleRepository.findByStatus(status);
    }
    public List<Schedule> getSchedulesByZoneId(Long zoneId) {
        return scheduleRepository.findByZone_Id(zoneId);
    }
    public List<Schedule> getSchedulesByStatus(Schedule.Status status) {
        return scheduleRepository.findByStatus(status);
    }
    public List<Schedule> getSchedulesByZoneIdAndStatus(Long zoneId, Schedule.Status status) {
        return scheduleRepository.findByZone_IdAndStatus(zoneId,status);
    }

}


