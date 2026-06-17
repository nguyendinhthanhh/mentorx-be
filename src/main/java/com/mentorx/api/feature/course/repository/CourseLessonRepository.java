package com.mentorx.api.feature.course.repository;

import com.mentorx.api.common.enums.LessonType;
import com.mentorx.api.feature.course.entity.CourseLesson;
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
public interface CourseLessonRepository extends JpaRepository<CourseLesson, UUID> {
    
    List<CourseLesson> findBySectionIdOrderByLessonOrderAsc(UUID sectionId);
    
    Page<CourseLesson> findBySectionId(UUID sectionId, Pageable pageable);
    
    Optional<CourseLesson> findBySectionIdAndLessonOrder(UUID sectionId, Integer lessonOrder);
    
    @Query("SELECT cl FROM CourseLesson cl WHERE cl.section.course.id = :courseId ORDER BY cl.section.sectionOrder, cl.lessonOrder")
    List<CourseLesson> findAllByCourseId(@Param("courseId") UUID courseId);
    
    @Query("SELECT COUNT(cl) FROM CourseLesson cl WHERE cl.section.id = :sectionId")
    Long countBySectionId(@Param("sectionId") UUID sectionId);
    
    @Query("SELECT COALESCE(MAX(cl.lessonOrder), 0) FROM CourseLesson cl WHERE cl.section.id = :sectionId")
    Integer findMaxLessonOrderBySectionId(@Param("sectionId") UUID sectionId);
    
    List<CourseLesson> findBySectionIdAndIsPublished(UUID sectionId, Boolean isPublished);
    
    List<CourseLesson> findBySectionIdAndIsFreePreview(UUID sectionId, Boolean isFreePreview);
    
    List<CourseLesson> findByLessonType(LessonType lessonType);
    
    @Query("SELECT cl FROM CourseLesson cl WHERE cl.section.course.id = :courseId AND cl.isFreePreview = true")
    List<CourseLesson> findFreePreviewLessonsByCourseId(@Param("courseId") UUID courseId);

    boolean existsBySectionIdAndLessonOrder(UUID sectionId, Integer lessonOrder);

    // M12.2 H0: required by CourseStatsServiceImpl — total view count across all lessons of a course
    @Query("SELECT COALESCE(SUM(cl.viewCount), 0) FROM CourseLesson cl WHERE cl.section.course.id = :courseId")
    Long sumViewCountByCourseId(@Param("courseId") UUID courseId);
}
