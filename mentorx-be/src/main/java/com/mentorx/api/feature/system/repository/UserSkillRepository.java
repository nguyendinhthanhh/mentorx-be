package com.mentorx.api.feature.system.repository;

import com.mentorx.api.feature.system.entity.UserSkill;
import com.mentorx.api.feature.system.entity.UserSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, UserSkillId> {
    List<UserSkill> findByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM UserSkill u WHERE u.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);
    List<UserSkill> findBySkillId(Integer skillId);
    boolean existsByUserIdAndSkillId(UUID userId, Integer skillId);
}
