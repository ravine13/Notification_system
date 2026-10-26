package com.NotificationSystem.services;

import com.NotificationSystem.entities.PasswordResetToken;
import com.NotificationSystem.entities.User;
import com.NotificationSystem.repositories.PasswordResetTokenRepository;
import com.NotificationSystem.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final PasswordEncoder encoder;
    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Transactional
    public void requestReset(String email) {
        userRepo.findByEmail(email).ifPresent(user -> {
            tokenRepo.deleteByUser(user);
            tokenRepo.flush();

            byte[] bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
            String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

            PasswordResetToken t = new PasswordResetToken();
            t.setUser(user);
            t.setTokenHash(sha256(rawToken));
            t.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
            tokenRepo.save(t);

            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setTo(user.getEmail());
                msg.setSubject("Reset your password");
                msg.setText("Hi " + user.getName() + ",\n\nUse this link to reset your password "
                        + "(valid for 30 minutes):\n"
                        + frontendUrl + "/reset-password?token=" + rawToken
                        + "\n\nIf you didn't request this, ignore this email.");
                mailSender.send(msg);
            } catch (Exception e) {
                // Don't leak failures to the caller (would reveal that the email exists)
                log.error("Failed to send reset email", e);
            }
        });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken t = tokenRepo.findByTokenHash(sha256(rawToken))
                .filter(x -> !x.isUsed() && x.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Invalid or expired token"));

        User user = t.getUser();
        user.setPassword(encoder.encode(newPassword));
        userRepo.save(user);
        t.setUsed(true);
        tokenRepo.save(t);
    }

    private String sha256(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256")
                    .digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}