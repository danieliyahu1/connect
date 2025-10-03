package com.akatsuki.auth.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Column(nullable = false,columnDefinition = "TEXT", unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private Instant issuedAt;

    @Column(nullable = false, updatable = false)
    private Instant expiresAt;

    public RefreshToken(String token, User user, Instant issuedAt, Instant expiresAt) {
        this.user = user;
        this.token = token;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

}
