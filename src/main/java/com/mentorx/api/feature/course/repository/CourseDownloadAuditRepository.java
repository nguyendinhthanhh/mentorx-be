package com.mentorx.api.feature.course.repository;

import com.mentorx.api.feature.course.entity.CourseDownloadAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CourseDownloadAuditRepository extends JpaRepository<CourseDownloadAudit, UUID> {
}
