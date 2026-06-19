package com.mentorx.api.feature.analytics.repository;

import com.mentorx.api.feature.analytics.entity.CourseDailySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseDailySnapshotRepository extends JpaRepository<CourseDailySnapshot, UUID> {

    List<CourseDailySnapshot> findByCourseIdOrderBySnapshotDateDesc(UUID courseId);

    List<CourseDailySnapshot> findByCourseIdAndSnapshotDateBetween(UUID courseId, LocalDate start, LocalDate end);

    Optional<CourseDailySnapshot> findByCourseIdAndSnapshotDate(UUID courseId, LocalDate snapshotDate);

    @org.springframework.data.jpa.repository.Query("""
        SELECT s FROM CourseDailySnapshot s
        WHERE s.course.instructor.id = :instructorId
        ORDER BY s.snapshotDate DESC
    """)
    List<CourseDailySnapshot> findAllForInstructor(@org.springframework.data.repository.query.Param("instructorId") UUID instructorId);
}
