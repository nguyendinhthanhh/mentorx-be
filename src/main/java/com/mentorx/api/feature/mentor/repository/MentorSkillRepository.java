package com.mentorx.api.feature.mentor.repository;

import com.mentorx.api.feature.mentor.entity.MentorSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MentorSkillRepository extends JpaRepository<MentorSkill, UUID> {

    List<MentorSkill> findByMentorProfileId(UUID mentorProfileId);

    List<MentorSkill> findByMentorProfileIdAndIsPrimary(UUID mentorProfileId, Boolean isPrimary);

    @Query("SELECT ms FROM MentorSkill ms WHERE ms.mentorProfile.id = :mentorProfileId " +
           "ORDER BY ms.proficiencyLevel DESC, ms.yearsOfExperience DESC")
    List<MentorSkill> findByMentorProfileIdOrderByProficiency(@Param("mentorProfileId") UUID mentorProfileId);

    @Query("SELECT DISTINCT ms.skillName FROM MentorSkill ms " +
           "WHERE LOWER(ms.skillName) LIKE LOWER(CONCAT('%', :skillName, '%')) " +
           "ORDER BY ms.skillName")
    List<String> findDistinctSkillNamesByPattern(@Param("skillName") String skillName);

    @Query("SELECT ms.skillName, COUNT(ms) as skillCount FROM MentorSkill ms " +
           "GROUP BY ms.skillName " +
           "ORDER BY skillCount DESC")
    List<Object[]> findSkillPopularity();

    void deleteByMentorProfileId(UUID mentorProfileId);
}