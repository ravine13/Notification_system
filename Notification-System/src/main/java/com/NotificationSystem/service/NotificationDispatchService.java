package com.NotificationSystem.service;

import com.NotificationSystem.entities.Notification;
import com.NotificationSystem.entities.Resident;
import com.NotificationSystem.entities.Schedule;
import com.NotificationSystem.repositories.NotificationRepository;
import com.NotificationSystem.repositories.ResidentRepository;
import com.NotificationSystem.repositories.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NotificationDispatchService {

    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private ResidentRepository residentRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private AfricaTalkingSmsService smsService;
    @Autowired
    private WhatsAppService whatsAppService;

    public int queueZoneNotifications(Long scheduleId, Notification.Channel channel) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        List<Resident> residents = residentRepository.findByZone_Id(schedule.getZone().getId());


        List<Notification> existing = notificationRepository.findBySchedule_Id(scheduleId);
        Set<Long> alreadyNotified = existing.stream()
                .map(n -> n.getResident().getId())
                .collect(Collectors.toSet());

        List<Resident> toNotify = residents.stream()
                .filter(r -> !alreadyNotified.contains(r.getId()))
                .toList();

        String messageText = "Waste collection in your zone is scheduled for "
                + schedule.getCollectionDate() + ". Please have your waste ready.";

        LocalDateTime now = LocalDateTime.now();

        List<Notification> queued = toNotify.stream().map(resident -> {
            Notification n = new Notification();
            n.setSchedule(schedule);
            n.setResident(resident);
            n.setPhoneNumber(resident.getPhoneNumber());
            n.setMessage(messageText);
            n.setChannel(channel);
            n.setStatus(Notification.Status.PENDING);
            n.setCreatedAt(now);
            n.setUpdatedAt(now);
            return n;
        }).toList();

        List<Notification> saved = notificationRepository.saveAll(queued);

        dispatchAsync(saved);

        return saved.size();
    }

    @Async
    public void dispatchAsync(List<Notification> notifications) {
        for (Notification n : notifications) {
            try {
                if (n.getChannel() == Notification.Channel.SMS) {
                    smsService.sendSms(n.getPhoneNumber(), n.getMessage());
                } else {
                    // CHANGED: use the approved template instead of free text.
                    // collectionDate is pulled from the schedule via the notification's message,
                    // but cleaner to pass it explicitly — see note below.
                    String collectionDate = n.getSchedule().getCollectionDate().toString();
                    whatsAppService.sendTemplateMessage(
                            n.getPhoneNumber(),
                            "collection_reminder",
                            "en",
                            collectionDate
                    );
                }
                n.setStatus(Notification.Status.SENT);
                n.setSentAt(LocalDateTime.now());
            } catch (Exception e) {
                n.setStatus(Notification.Status.FAILED);
            }
            n.setUpdatedAt(LocalDateTime.now());
            notificationRepository.save(n);

            try {
                Thread.sleep(250);
            } catch (InterruptedException ignored) {
            }
        }
    }
}