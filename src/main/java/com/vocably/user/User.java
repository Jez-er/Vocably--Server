package com.vocably.user;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String displayName;

    /** Null for a federated account, which has no password to check. */
    @Column(nullable = true)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider = AuthProvider.LOCAL;

    /** The provider's own user id; null for {@link AuthProvider#LOCAL}. */
    @Column(name = "provider_id")
    private String providerId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    /** Whether this account can be signed into with a password. */
    public boolean hasPassword() {
        return passwordHash != null && !passwordHash.isBlank();
    }
}
