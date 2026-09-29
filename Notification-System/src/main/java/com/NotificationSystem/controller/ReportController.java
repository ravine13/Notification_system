package com.NotificationSystem.controller;

import com.NotificationSystem.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/collection-performance")
    public ResponseEntity<Map<String, Object>> getCollectionPerformance(
            @RequestParam(required = false) String range) {
        return ResponseEntity.ok(reportService.getCollectionPerformance(range));
    }

    @GetMapping("/notification-delivery")
    public ResponseEntity<Map<String, Object>> getNotificationDelivery(
            @RequestParam(required = false) String range) {
        return ResponseEntity.ok(reportService.getNotificationDelivery(range));
    }
}