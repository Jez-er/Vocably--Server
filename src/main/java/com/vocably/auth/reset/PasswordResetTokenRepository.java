package com.vocably.auth.reset;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /** Issuing a new link invalidates any outstanding one for that user. */
    @Modifying
    @Query("delete from PasswordResetToken t where t.userId = :userId and t.usedAt is null")
    void deleteUnusedByUserId(@Param("userId") UUID userId);
}
