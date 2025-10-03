package com.akatsuki.auth.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;
import java.util.UUID;
import com.akatsuki.auth.enums.AuthProvider;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Setter(AccessLevel.NONE)
    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    @Column(unique = true,nullable = false)
    private String email;

    @Column(name = "encoded_password")
    private String encodedPassword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    private String providerUserId;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public User (String email, String encodedPassword, AuthProvider authProvider) {
        this.email = email;
        this.encodedPassword = encodedPassword;
        this.provider = authProvider;
        setUserId();    }

    public User(String email,AuthProvider authProvider, String providerUserId) {
        this.email = email;
        this.provider = authProvider;
        this.providerUserId = providerUserId;
        setUserId();
    }

    private void setUserId() {
        this.userId = UUID.randomUUID();
    }
}
