package com.mentorx.api.feature.user.repository;

import com.mentorx.api.feature.user.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByTokenAndIsUsedFalse(String token);

    List<EmailVerificationToken> findByUserIdAndIsUsedFalse(UUID userId);

    long countByUserIdAndIsUsedFalse(UUID userId);

    @Modifying
    @Query("UPDATE EmailVerificationToken e SET e.isUsed = true, e.usedAt = CURRENT_TIMESTAMP WHERE e.user.id = :userId AND e.isUsed = false")
    int invalidatePreviousTokens(@Param("userId") UUID userId);
}
