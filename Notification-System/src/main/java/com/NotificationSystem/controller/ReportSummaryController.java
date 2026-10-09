package com.NotificationSystem.controller;

import com.NotificationSystem.dto.ReportDtos.*;
import com.NotificationSystem.service.ReportSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Admin-only reporting endpoints. Access is enforced in SecurityConfig with
 *   .requestMatchers("/reports/**").hasRole("ADMIN")
 *
 * All endpoints take optional ?from=YYYY-MM-DD&to=YYYY-MM-DD.
 * Default range: the last 30 days, ending today (Nairobi time).
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportSummaryController {

    private static final ZoneId NAIROBI = ZoneId.of("Africa/Nairobi");
    private static final long MAX_RANGE_DAYS = 366;

    private final ReportSummaryService reportService;

    @GetMapping("/summary")
    public Summary summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate[] r = resolve(from, to);
        return reportService.summary(r[0], r[1]);
    }

    @GetMapping("/zones")
    public List<ZoneRow> zones(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate[] r = resolve(from, to);
        return reportService.zones(r[0], r[1]);
    }

    @GetMapping("/trucks")
    public TrucksReport trucks(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate[] r = resolve(from, to);
        return reportService.trucks(r[0], r[1]);
    }

    private LocalDate[] resolve(LocalDate from, LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now(NAIROBI);
        LocalDate start = from != null ? from : end.minusDays(29);

        if (start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' must be on or before 'to'");
        }
        if (ChronoUnit.DAYS.between(start, end) > MAX_RANGE_DAYS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date range cannot be longer than one year");
        }
        return new LocalDate[]{start, end};
    }
}