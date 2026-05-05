package com.mentorx.api.feature.system.repository;

import com.mentorx.api.feature.system.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Integer> {
    Optional<Skill> findBySlug(String slug);
    
    List<Skill> findByIsActiveTrueOrderBySlugAsc();
    
    List<Skill> findByIsActiveTrueOrderByLabelEnAsc();
    
    List<Skill> findByLabelEnContainingIgnoreCaseOrLabelViContainingIgnoreCase(String labelEn, String labelVi);
    
    boolean existsBySlug(String slug);
}
