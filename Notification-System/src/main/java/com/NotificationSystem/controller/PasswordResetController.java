package com.NotificationSystem.controllers;

import com.NotificationSystem.services.PasswordResetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService service;

    public record ForgotRequest(@Email @NotBlank String email) {}
    public record ResetRequest(@NotBlank String token,
                               @NotBlank @Size(min = 8) String newPassword) {}

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgot(@Valid @RequestBody ForgotRequest r) {
        service.requestReset(r.email());
        return ResponseEntity.ok(Map.of("message",
                "If that email exists, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> reset(@Valid @RequestBody ResetRequest r) {
        service.resetPassword(r.token(), r.newPassword());
        return ResponseEntity.ok(Map.of("message", "Password updated."));
    }
}