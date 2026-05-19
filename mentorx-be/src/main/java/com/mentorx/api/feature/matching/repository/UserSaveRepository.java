package com.mentorx.api.feature.matching.repository;

import com.mentorx.api.feature.matching.entity.UserSave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSaveRepository extends JpaRepository<UserSave, UUID> {

    Optional<UserSave> findByUserIdAndTargetTypeAndTargetId(UUID userId, String targetType, UUID targetId);

    boolean existsByUserIdAndTargetTypeAndTargetId(UUID userId, String targetType, UUID targetId);

    List<UserSave> findByUserIdAndTargetTypeOrderBySavedAtDesc(UUID userId, String targetType);

    void deleteByUserIdAndTargetTypeAndTargetId(UUID userId, String targetType, UUID targetId);
}
