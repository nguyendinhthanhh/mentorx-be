package com.mentorx.api.feature.course.repository;

import com.mentorx.api.feature.course.entity.CourseSection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseSectionRepository extends JpaRepository<CourseSection, UUID> {
    
    List<CourseSection> findByCourseIdOrderBySectionOrderAsc(UUID courseId);
    
    Page<CourseSection> findByCourseId(UUID courseId, Pageable pageable);
    
    Optional<CourseSection> findByCourseIdAndSectionOrder(UUID courseId, Integer sectionOrder);
    
    @Query("SELECT COUNT(cs) FROM CourseSection cs WHERE cs.course.id = :courseId")
    Long countByCourseId(@Param("courseId") UUID courseId);
    
    @Query("SELECT COALESCE(MAX(cs.sectionOrder), 0) FROM CourseSection cs WHERE cs.course.id = :courseId")
    Integer findMaxSectionOrderByCourseId(@Param("courseId") UUID courseId);
    
    List<CourseSection> findByCourseIdAndIsPublished(UUID courseId, Boolean isPublished);
    
    boolean existsByCourseIdAndSectionOrder(UUID courseId, Integer sectionOrder);
}
