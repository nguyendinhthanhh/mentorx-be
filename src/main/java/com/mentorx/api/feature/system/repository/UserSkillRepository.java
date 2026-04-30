package com.mentorx.api.feature.system.repository;

import com.mentorx.api.feature.system.entity.UserSkill;
import com.mentorx.api.feature.system.entity.UserSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, UserSkillId> {
    List<UserSkill> findByUserId(UUID userId);
    List<UserSkill> findBySkillId(Integer skillId);
    boolean existsByUserIdAndSkillId(UUID userId, Integer skillId);
}
