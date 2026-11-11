package com.NotificationSystem.service;

import com.NotificationSystem.entities.Notification;
import com.NotificationSystem.entities.Resident;
import com.NotificationSystem.entities.Schedule;
import com.NotificationSystem.repositories.NotificationRepository;
import com.NotificationSystem.repositories.ResidentRepository;
import com.NotificationSystem.repositories.ScheduleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    @Value("${whatsapp.mode:text}")
    private String whatsappMode;

    public Map<String, Object> queueZoneNotifications(Long scheduleId, Notification.Channel channel) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        List<Resident> residents = residentRepository.findByZone_Id(schedule.getZone().getId());
        List<Notification> existing = notificationRepository.findBySchedule_Id(scheduleId);
        Set<Long> alreadyNotified = existing.stream()
                .filter(n -> n.getChannel() == channel && n.getStatus() != Notification.Status.FAILED)
                .map(n -> n.getResident().getId())
                .collect(Collectors.toSet());

        List<Resident> toNotify = residents.stream()
                .filter(r -> {
                    boolean duplicate = alreadyNotified.contains(r.getId());
                    if (duplicate) {
                        System.out.println("Duplicate prevented: schedule "
                                + scheduleId + ", resident " + r.getId());
                    }
                    return !duplicate;
                })
                .toList();

        String zoneName = schedule.getZone().getName();
        LocalDateTime now = LocalDateTime.now();

        List<Notification> queued = toNotify.stream().map(resident -> {
            String name = resident.getName() == null ? "" : resident.getName().trim();
            String firstName = name.isEmpty() ? "resident" : name.split("\\s+")[0];

            String messageText = "Hello " + firstName + ", waste collection for "
                    + zoneName + " zone is scheduled on "
                    + schedule.getCollectionDate() + ". Please have your waste ready.";

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

        int duplicatesPrevented = residents.size() - toNotify.size();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("queued", saved.size());
        result.put("duplicatesPrevented", duplicatesPrevented);
        result.put("message", duplicatesPrevented > 0
                ? "Duplicate prevented: " + duplicatesPrevented
                + " resident(s) already notified for this schedule"
                : "All residents queued");
        return result;
    }

    @Async
    public void dispatchAsync(List<Notification> notifications) {
        for (Notification n : notifications) {
            try {
                if (n.getChannel() == Notification.Channel.SMS) {
                    String response = smsService.sendSms(n.getPhoneNumber(), n.getMessage());
                    n.setWhatsappMessageId(extractSmsMessageId(response));
                } else {
                    String response;
                    if ("template".equalsIgnoreCase(whatsappMode)) {
                        String collectionDate = n.getSchedule().getCollectionDate().toString();
                        response = whatsAppService.sendTemplateMessage(
                                n.getPhoneNumber(),
                                "collection_reminder",
                                "en",
                                collectionDate
                        );
                    } else {
                        response = whatsAppService.sendMessage(n.getPhoneNumber(), n.getMessage());
                    }
                    n.setWhatsappMessageId(extractWhatsAppMessageId(response));
                }
                n.setStatus(Notification.Status.SENT);
                n.setSentAt(LocalDateTime.now());
            } catch (Exception e) {
                String code = (e instanceof MessagingException me) ? me.getCode() : null;
                String msg = e.getMessage();

                System.out.println("Dispatch Failed for " + n.getPhoneNumber() + ": " + msg);
                n.setStatus(Notification.Status.FAILED);
                n.setErrorCode(code);
                n.setErrorDetail(msg != null && msg.length() > 250 ? msg.substring(0, 250) : msg);
            }
            n.setUpdatedAt(LocalDateTime.now());
            notificationRepository.save(n);

            try {
                Thread.sleep(250);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private String extractWhatsAppMessageId(String json) {
        try {
            return new ObjectMapper().readTree(json)
                    .path("messages").path(0).path("id").asText(null);
        } catch (Exception e) {
            return null;
        }
    }


    private String extractSmsMessageId(String json) {
        try {
            return new ObjectMapper().readTree(json)
                    .path("SMSMessageData").path("Recipients").path(0).path("messageId").asText(null);
        } catch (Exception e) {
            return null;
        }
    }
}