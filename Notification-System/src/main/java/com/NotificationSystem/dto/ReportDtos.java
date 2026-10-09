package com.NotificationSystem.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * JSON shapes returned by /reports/*.
 * Rates are fractions between 0 and 1 (0.7 = 70%) and are null when there is
 * nothing to divide by, so the UI can show a dash instead of a misleading 0%.
 */
public final class ReportDtos {

    private ReportDtos() {}

    public record Range(LocalDate from, LocalDate to) {}

    // ---- /reports/summary ----
    public record Summary(
            Range range,
            SchedulesSection schedules,
            NotificationsSection notifications,
            CollectionsSection collections,
            CoverageSection coverage
    ) {}

    public record SchedulesSection(
            long total,
            Map<String, Long> byStatus,
            Double completionRate,          // COMPLETED / (total - CANCELLED)
            List<MonthPoint> byMonth
    ) {}

    public record MonthPoint(String month, long total, long completed) {} // month = "2026-09"

    public record NotificationsSection(
            long total,
            long wentOut,                   // SENT + DELIVERED + READ
            long pending,
            long failed,
            Double failureRate,             // FAILED / (total - PENDING)
            Map<String, Long> byStatus,
            Map<String, Long> byChannel,
            List<FailureReason> topFailures
    ) {}

    public record FailureReason(String detail, long count) {}

    public record CollectionsSection(
            long total,
            long collected,
            long missed,
            long pending,
            Double collectionRate           // COLLECTED / (COLLECTED + MISSED)
    ) {}

    public record CoverageSection(long zones, long zonesWithoutDay, long residents) {}

    // ---- /reports/zones ----
    public record ZoneRow(
            Long id,
            String name,
            String collectionDay,
            long residents,
            long schedules,                 // excludes cancelled
            long completed,
            Double completionRate,
            long collected,
            long missed
    ) {}

    // ---- /reports/trucks ----
    public record TruckRow(
            Long id,
            String plateNumber,
            String driverName,
            String status,
            long schedules,                 // excludes cancelled
            long completed,
            Double completionRate
    ) {}

    public record TrucksReport(Range range, List<TruckRow> trucks, long unassignedSchedules) {}
}