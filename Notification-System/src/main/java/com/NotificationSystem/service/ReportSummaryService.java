package com.NotificationSystem.service;

import com.NotificationSystem.dto.ReportDtos.*;
import com.NotificationSystem.entities.Collection;
import com.NotificationSystem.entities.Notification;
import com.NotificationSystem.entities.Schedule;
import com.NotificationSystem.repositories.ReportRepository;
import com.NotificationSystem.repositories.ReportRepository.MonthCount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportSummaryService {

    private final ReportRepository repo;


    public Summary summary(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay(); // exclusive, so "to" is included

        return new Summary(
                new Range(from, to),
                schedules(from, to),
                notifications(start, end),
                collections(from, to),
                coverage()
        );
    }

    private SchedulesSection schedules(LocalDate from, LocalDate to) {
        Map<String, Long> byStatus = zeroed(Schedule.Status.values());
        repo.schedulesByStatus(from, to)
                .forEach(r -> byStatus.put(r.getStatus().name(), n(r.getTotal())));

        long total = sum(byStatus);
        long cancelled = byStatus.get("CANCELLED");
        long completed = byStatus.get("COMPLETED");

        List<MonthPoint> byMonth = repo
                .schedulesByMonth(from, to, Schedule.Status.COMPLETED, Schedule.Status.CANCELLED)
                .stream()
                .sorted(Comparator.comparing(MonthCount::getYr).thenComparing(MonthCount::getMo))
                .map(m -> new MonthPoint(
                        String.format("%04d-%02d", m.getYr(), m.getMo()),
                        n(m.getTotal()),
                        n(m.getCompleted())))
                .toList();

        return new SchedulesSection(total, byStatus, rate(completed, total - cancelled), byMonth);
    }

    private NotificationsSection notifications(LocalDateTime start, LocalDateTime end) {
        Map<String, Long> byStatus = zeroed(Notification.Status.values());
        repo.notificationsByStatus(start, end)
                .forEach(r -> byStatus.put(r.getStatus().name(), n(r.getTotal())));

        Map<String, Long> byChannel = zeroed(Notification.Channel.values());
        repo.notificationsByChannel(start, end)
                .forEach(r -> byChannel.put(r.getChannel().name(), n(r.getTotal())));

        long total = sum(byStatus);
        long pending = byStatus.get("PENDING");
        long failed = byStatus.get("FAILED");
        long wentOut = byStatus.get("SENT") + byStatus.get("DELIVERED") + byStatus.get("READ");

        List<FailureReason> topFailures = repo
                .notificationFailures(Notification.Status.FAILED, start, end)
                .stream()
                .limit(5)
                .map(f -> new FailureReason(f.getDetail(), n(f.getTotal())))
                .toList();

        return new NotificationsSection(
                total, wentOut, pending, failed,
                rate(failed, total - pending),
                byStatus, byChannel, topFailures);
    }

    private CollectionsSection collections(LocalDate from, LocalDate to) {
        Map<String, Long> byStatus = zeroed(Collection.Status.values());
        repo.collectionsByStatus(from, to)
                .forEach(r -> byStatus.put(r.getStatus().name(), n(r.getTotal())));

        long collected = byStatus.get("COLLECTED");
        long missed = byStatus.get("MISSED");
        long pending = byStatus.get("PENDING");

        return new CollectionsSection(
                collected + missed + pending, collected, missed, pending,
                rate(collected, collected + missed));
    }

    private CoverageSection coverage() {
        return new CoverageSection(
                n(repo.countZones()),
                n(repo.countZonesWithoutDay()),
                n(repo.countResidents()));
    }


    public List<ZoneRow> zones(LocalDate from, LocalDate to) {
        Map<Long, Long> residents = new HashMap<>();
        repo.residentsPerZone().forEach(r -> residents.put(r.getZoneId(), n(r.getTotal())));

        // [0] = non-cancelled schedules, [1] = completed
        Map<Long, long[]> schedules = new HashMap<>();
        repo.schedulesByZone(from, to).forEach(r -> {
            if (r.getStatus() == Schedule.Status.CANCELLED) return;
            long[] a = schedules.computeIfAbsent(r.getZoneId(), k -> new long[2]);
            a[0] += n(r.getTotal());
            if (r.getStatus() == Schedule.Status.COMPLETED) a[1] += n(r.getTotal());
        });

        // [0] = collected, [1] = missed
        Map<Long, long[]> collections = new HashMap<>();
        repo.collectionsByZone(from, to).forEach(r -> {
            long[] a = collections.computeIfAbsent(r.getZoneId(), k -> new long[2]);
            if (r.getStatus() == Collection.Status.COLLECTED) a[0] += n(r.getTotal());
            if (r.getStatus() == Collection.Status.MISSED) a[1] += n(r.getTotal());
        });

        return repo.zoneList().stream().map(z -> {
            long[] s = schedules.getOrDefault(z.getId(), new long[2]);
            long[] c = collections.getOrDefault(z.getId(), new long[2]);
            return new ZoneRow(
                    z.getId(), z.getName(), z.getCollectionDay(),
                    residents.getOrDefault(z.getId(), 0L),
                    s[0], s[1], rate(s[1], s[0]),
                    c[0], c[1]);
        }).toList();
    }


    public TrucksReport trucks(LocalDate from, LocalDate to) {
        // [0] = non-cancelled schedules, [1] = completed
        Map<Long, long[]> schedules = new HashMap<>();
        repo.schedulesByTruck(from, to).forEach(r -> {
            if (r.getStatus() == Schedule.Status.CANCELLED) return;
            long[] a = schedules.computeIfAbsent(r.getTruckId(), k -> new long[2]);
            a[0] += n(r.getTotal());
            if (r.getStatus() == Schedule.Status.COMPLETED) a[1] += n(r.getTotal());
        });

        List<TruckRow> rows = repo.truckList().stream().map(t -> {
            long[] s = schedules.getOrDefault(t.getId(), new long[2]);
            return new TruckRow(
                    t.getId(), t.getPlateNumber(), t.getDriverName(),
                    t.getStatus().name(),
                    s[0], s[1], rate(s[1], s[0]));
        }).toList();

        return new TrucksReport(
                new Range(from, to),
                rows,
                n(repo.countUnassignedSchedules(from, to, Schedule.Status.CANCELLED)));
    }


    private static <E extends Enum<E>> Map<String, Long> zeroed(E[] values) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (E v : values) map.put(v.name(), 0L);
        return map;
    }

    private static long sum(Map<String, Long> map) {
        return map.values().stream().mapToLong(Long::longValue).sum();
    }

    private static long n(Long value) {
        return value == null ? 0L : value;
    }


    private static Double rate(long numerator, long denominator) {
        if (denominator <= 0) return null;
        return Math.round(numerator * 10000.0 / denominator) / 10000.0;
    }
}