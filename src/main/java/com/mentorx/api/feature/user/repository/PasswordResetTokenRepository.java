package com.mentorx.api.feature.user.repository;

import com.mentorx.api.feature.user.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByToken(String token);

    @Modifying
    @Query("""
            UPDATE PasswordResetToken t
            SET t.isInvalidated = true, t.invalidatedAt = CURRENT_TIMESTAMP, t.invalidationReason = :reason
            WHERE t.user.id = :userId
              AND t.isUsed = false
              AND t.isInvalidated = false
            """)
    void invalidateActiveTokensByUserId(@Param("userId") UUID userId, @Param("reason") String reason);
}
