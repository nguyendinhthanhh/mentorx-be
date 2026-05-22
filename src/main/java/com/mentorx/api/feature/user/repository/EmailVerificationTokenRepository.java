package com.mentorx.api.feature.user.repository;

import com.mentorx.api.feature.user.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByToken(String token);

    @Modifying
    @Query("UPDATE EmailVerificationToken t SET t.isUsed = true, t.usedAt = CURRENT_TIMESTAMP WHERE t.user.id = :userId AND t.isUsed = false")
    void invalidateActiveTokensByUserId(@Param("userId") UUID userId);
}

