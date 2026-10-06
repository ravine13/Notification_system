package com.NotificationSystem.controller;

import com.NotificationSystem.entities.Notification;
import com.NotificationSystem.repositories.NotificationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/webhook/whatsapp")
public class WhatsAppWebhookController {

    @Value("${whatsapp.verify-token}")
    private String verifyToken;

    @Autowired
    private NotificationRepository notificationRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    // Meta calls this once to check you own the URL
    @GetMapping
    public ResponseEntity<String> verify(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(403).build();
    }

    // Meta sends delivery updates here
    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody String payload) {
        System.out.println("WhatsApp webhook: " + payload);
        try {
            JsonNode root = mapper.readTree(payload);
            for (JsonNode entry : root.path("entry")) {
                for (JsonNode change : entry.path("changes")) {
                    for (JsonNode s : change.path("value").path("statuses")) {
                        handleStatus(s);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Webhook parse error: " + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    private void handleStatus(JsonNode s) {
        String wamid = s.path("id").asText();
        String status = s.path("status").asText();

        notificationRepository.findByWhatsappMessageId(wamid).ifPresent(n -> {
            Notification.Status current = n.getStatus();

            switch (status) {
                case "delivered" -> {
                    if (current != Notification.Status.READ) {
                        n.setStatus(Notification.Status.DELIVERED);
                    }
                }
                case "read" -> n.setStatus(Notification.Status.READ);
                case "failed" -> {
                    n.setStatus(Notification.Status.FAILED);
                    JsonNode err = s.path("errors").path(0);
                    n.setErrorDetail(err.path("code").asText() + ": " + err.path("title").asText());
                }
                default -> { return; }
            }
            n.setUpdatedAt(LocalDateTime.now());
            notificationRepository.save(n);
        });
    }
}