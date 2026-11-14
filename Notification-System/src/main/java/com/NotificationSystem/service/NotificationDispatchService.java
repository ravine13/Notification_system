package com.NotificationSystem.service;

import com.NotificationSystem.entities.Notification;
import com.NotificationSystem.entities.Resident;
import com.NotificationSystem.entities.Schedule;
import com.NotificationSystem.repositories.NotificationRepository;
import com.NotificationSystem.repositories.ResidentRepository;
import com.NotificationSystem.repositories.ScheduleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
                        log.info("Dispatch: skipped resident {} on schedule {} ({}), already notified",
                                r.getId(), scheduleId, channel);
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

        int duplicatesPrevented = residents.size() - toNotify.size();

        log.info("Dispatch: schedule {} via {}: {} residents in zone, {} queued, {} skipped",
                scheduleId, channel, residents.size(), saved.size(), duplicatesPrevented);

        dispatchAsync(saved);

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
        int sent = 0;
        int failed = 0;

        for (Notification n : notifications) {
            log.info("Dispatch: notification {} to resident {} via {} (thread {})",
                    n.getId(), n.getResident().getId(), n.getChannel(),
                    Thread.currentThread().getName());
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
                sent++;
            } catch (Exception e) {
                String code = (e instanceof MessagingException me) ? me.getCode() : null;
                String msg = e.getMessage();

                if (e instanceof MessagingException) {
                    log.warn("Dispatch: notification {} FAILED [{}]: {}", n.getId(), code, msg);
                } else {
                    log.error("Dispatch: notification {} FAILED with an unexpected error", n.getId(), e);
                }

                n.setStatus(Notification.Status.FAILED);
                n.setErrorCode(code);
                n.setErrorDetail(msg != null && msg.length() > 250 ? msg.substring(0, 250) : msg);
                failed++;
            }
            n.setUpdatedAt(LocalDateTime.now());
            notificationRepository.save(n);

            try {
                Thread.sleep(250);
            } catch (InterruptedException ignored) {
            }
        }

        log.info("Dispatch: batch finished: {} sent, {} failed, {} total",
                sent, failed, notifications.size());
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