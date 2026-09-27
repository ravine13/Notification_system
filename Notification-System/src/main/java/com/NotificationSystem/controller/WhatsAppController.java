package com.NotificationSystem.controller;

import com.NotificationSystem.service.WhatsAppService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppController {

    private final WhatsAppService whatsappService;

    public WhatsAppController(WhatsAppService whatsappService) {
        this.whatsappService = whatsappService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(
            @RequestParam String phone,
            @RequestParam String message) {

        if (phone == null || phone.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Phone number is required");
        }

        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Message is required");
        }

        if (!phone.matches("\\d{10,15}")) {
            return ResponseEntity.badRequest()
                    .body("Phone number must contain 10 to 15 digits without +");
        }

        return ResponseEntity.ok(
                whatsappService.sendMessage(phone, message)
        );
    }
}