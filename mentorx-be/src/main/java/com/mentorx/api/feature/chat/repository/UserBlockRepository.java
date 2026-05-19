package com.mentorx.api.feature.chat.repository;

import com.mentorx.api.feature.chat.entity.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, UUID> {
    Optional<UserBlock> findByBlockerUserIdAndBlockedUserId(UUID blockerUserId, UUID blockedUserId);
}
