package com.NotificationSystem.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "password_reset_tokens")
@Getter @Setter @NoArgsConstructor
public class PasswordResetToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean used = false;
}