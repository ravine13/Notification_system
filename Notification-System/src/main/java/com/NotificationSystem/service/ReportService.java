package com.NotificationSystem.service;

import com.NotificationSystem.entities.Collection;
import com.NotificationSystem.entities.Notification;
import com.NotificationSystem.entities.Schedule;
import com.NotificationSystem.entities.Zone;
import com.NotificationSystem.repositories.CollectionRepository;
import com.NotificationSystem.repositories.NotificationRepository;
import com.NotificationSystem.repositories.ScheduleRepository;
import com.NotificationSystem.repositories.ZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired private CollectionRepository collectionRepository;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private ZoneRepository zoneRepository;
    @Autowired private NotificationRepository notificationRepository;

    public Map<String, Object> getCollectionPerformance(String range) {
        List<Collection> collections = collectionRepository.findAll();

        if ("month".equalsIgnoreCase(range)) {
            YearMonth current = YearMonth.now();
            collections = collections.stream()
                    .filter(c -> c.getCreatedAt() != null
                            && YearMonth.from(c.getCreatedAt()).equals(current))
                    .toList();
        }

        // scheduleId -> zoneId, zoneId -> zoneName (built once, used for grouping)
        Map<Long, Schedule> schedulesById = scheduleRepository.findAll().stream()
                .collect(Collectors.toMap(Schedule::getId, s -> s));
        Map<Long, String> zoneNamesById = zoneRepository.findAll().stream()
                .collect(Collectors.toMap(Zone::getId, Zone::getName));

        int totalCollected = 0, totalMissed = 0, totalPending = 0;

        // zoneId -> [collected, missed, pending]
        Map<Long, int[]> byZoneCounts = new LinkedHashMap<>();

        for (Collection c : collections) {
            Schedule schedule = schedulesById.get(c.getScheduleId());
            Long zoneId = schedule != null && schedule.getZone() != null ? schedule.getZone().getId() : null;

            int[] counts = byZoneCounts.computeIfAbsent(zoneId, k -> new int[3]);

            switch (c.getStatus()) {
                case COLLECTED -> { totalCollected++; counts[0]++; }
                case MISSED -> { totalMissed++; counts[1]++; }
                case PENDING -> { totalPending++; counts[2]++; }
            }
        }

        int totalCollections = collections.size();
        double collectionRate = totalCollections == 0 ? 0.0
                : round1((totalCollected * 100.0) / totalCollections);

        List<Map<String, Object>> byZone = new ArrayList<>();
        for (Map.Entry<Long, int[]> entry : byZoneCounts.entrySet()) {
            Long zoneId = entry.getKey();
            int[] counts = entry.getValue();
            int zoneTotal = counts[0] + counts[1] + counts[2];
            double zoneRate = zoneTotal == 0 ? 0.0 : round1((counts[0] * 100.0) / zoneTotal);

            Map<String, Object> zoneReport = new LinkedHashMap<>();
            zoneReport.put("zoneId", zoneId);
            zoneReport.put("zoneName", zoneId != null ? zoneNamesById.getOrDefault(zoneId, "Unknown") : "Unassigned");
            zoneReport.put("collected", counts[0]);
            zoneReport.put("missed", counts[1]);
            zoneReport.put("pending", counts[2]);
            zoneReport.put("collectionRate", zoneRate);
            byZone.add(zoneReport);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("range", "month".equalsIgnoreCase(range) ? "month" : "all-time");
        result.put("totalCollections", totalCollections);
        result.put("collected", totalCollected);
        result.put("missed", totalMissed);
        result.put("pending", totalPending);
        result.put("collectionRate", collectionRate);
        result.put("byZone", byZone);
        return result;
    }

    public Map<String, Object> getNotificationDelivery(String range) {
        List<Notification> notifications = notificationRepository.findAll();

        if ("month".equalsIgnoreCase(range)) {
            YearMonth current = YearMonth.now();
            notifications = notifications.stream()
                    .filter(n -> n.getCreatedAt() != null
                            && YearMonth.from(n.getCreatedAt()).equals(current))
                    .toList();
        }

        int totalSent = 0, totalFailed = 0, totalPending = 0;
        Map<Notification.Channel, int[]> byChannelCounts = new LinkedHashMap<>();

        for (Notification n : notifications) {
            int[] counts = byChannelCounts.computeIfAbsent(n.getChannel(), k -> new int[3]);
            switch (n.getStatus()) {
                case SENT -> { totalSent++; counts[0]++; }
                case FAILED -> { totalFailed++; counts[1]++; }
                case PENDING -> { totalPending++; counts[2]++; }
            }
        }

        int totalNotifications = notifications.size();
        double deliveryRate = totalNotifications == 0 ? 0.0
                : round1((totalSent * 100.0) / totalNotifications);

        List<Map<String, Object>> byChannel = new ArrayList<>();
        for (Map.Entry<Notification.Channel, int[]> entry : byChannelCounts.entrySet()) {
            int[] counts = entry.getValue();
            int channelTotal = counts[0] + counts[1] + counts[2];
            double channelRate = channelTotal == 0 ? 0.0 : round1((counts[0] * 100.0) / channelTotal);

            Map<String, Object> channelReport = new LinkedHashMap<>();
            channelReport.put("channel", entry.getKey().name());
            channelReport.put("sent", counts[0]);
            channelReport.put("failed", counts[1]);
            channelReport.put("pending", counts[2]);
            channelReport.put("deliveryRate", channelRate);
            byChannel.add(channelReport);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("range", "month".equalsIgnoreCase(range) ? "month" : "all-time");
        result.put("totalNotifications", totalNotifications);
        result.put("sent", totalSent);
        result.put("failed", totalFailed);
        result.put("pending", totalPending);
        result.put("deliveryRate", deliveryRate);
        result.put("byChannel", byChannel);
        return result;
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}