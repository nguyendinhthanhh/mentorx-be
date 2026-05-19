package com.mentorx.api.feature.course.repository;

import com.mentorx.api.feature.course.entity.LessonComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LessonCommentRepository extends JpaRepository<LessonComment, UUID> {
    
    Page<LessonComment> findByLessonIdAndParentIsNullAndIsDeletedFalse(UUID lessonId, Pageable pageable);
    
    List<LessonComment> findByParentIdAndIsDeletedFalse(UUID parentId);
    
    @Query("SELECT COUNT(lc) FROM LessonComment lc WHERE lc.lesson.id = :lessonId AND lc.isDeleted = false")
    Long countByLessonId(@Param("lessonId") UUID lessonId);
    
    @Query("SELECT lc FROM LessonComment lc WHERE lc.user.id = :userId AND lc.isDeleted = false")
    Page<LessonComment> findByUserId(@Param("userId") UUID userId, Pageable pageable);
    
    @Query("SELECT lc FROM LessonComment lc WHERE lc.lesson.section.course.id = :courseId AND lc.isDeleted = false")
    Page<LessonComment> findByCourseId(@Param("courseId") UUID courseId, Pageable pageable);
}
