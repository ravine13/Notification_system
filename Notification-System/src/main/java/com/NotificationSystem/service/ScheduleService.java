package com.NotificationSystem.service;

import com.NotificationSystem.entities.Schedule;
import com.NotificationSystem.entities.Truck;
import com.NotificationSystem.repositories.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    public Optional<Schedule> getScheduleById(Long id) {
        return scheduleRepository.findById(id);
    }

    // Create or update schedule
    public Schedule saveSchedule(Schedule schedule) {
        checkTruckAvailability(schedule);

        LocalDateTime now = LocalDateTime.now();
        if (schedule.getId() == null) {
            schedule.setCreatedAt(now);
        }
        schedule.setUpdatedAt(now);
        return scheduleRepository.save(schedule);
    }


    private void checkTruckAvailability(Schedule schedule) {
        Truck truck = schedule.getTruck();
        if (truck == null || truck.getId() == null) return;
        if (schedule.getStatus() == Schedule.Status.CANCELLED) return;

        boolean clash = scheduleRepository
                .findByTruck_IdAndCollectionDate(truck.getId(), schedule.getCollectionDate())
                .stream()
                .anyMatch(s -> !Objects.equals(s.getId(), schedule.getId())
                        && s.getStatus() != Schedule.Status.CANCELLED);

        if (clash) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Truck already booked on that date");
        }
    }

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
        return scheduleRepository.findByZone_IdAndStatus(zoneId, status);
    }
}