package com.NotificationSystem.controller;

import com.NotificationSystem.entities.Notification;
import com.NotificationSystem.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/webhook/sms")
public class SmsWebhookController {

    @Autowired
    private NotificationRepository notificationRepository;

    // Africa's Talking posts delivery reports as form data
    @PostMapping
    public ResponseEntity<Void> receive(@RequestParam Map<String, String> params) {
        System.out.println("SMS delivery report: " + params);

        String id = params.get("id");
        String status = params.getOrDefault("status", "");
        String reason = params.get("failureReason");

        if (id != null) {
            notificationRepository.findByWhatsappMessageId(id).ifPresent(n -> {
                switch (status.toLowerCase()) {
                    case "success" -> {
                        if (n.getStatus() != Notification.Status.READ) {
                            n.setStatus(Notification.Status.DELIVERED);
                        }
                    }
                    case "failed", "rejected" -> {
                        n.setStatus(Notification.Status.FAILED);
                        n.setErrorDetail(reason != null ? reason : status);
                    }
                    default -> { return; }   // sent, buffered, submitted: still in transit
                }
                n.setUpdatedAt(LocalDateTime.now());
                notificationRepository.save(n);
            });
        }
        return ResponseEntity.ok().build();
    }
}