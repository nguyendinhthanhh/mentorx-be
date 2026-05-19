package com.mentorx.api.feature.mentor.repository;

import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.feature.mentor.entity.MentorOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MentorOfferingRepository extends JpaRepository<MentorOffering, UUID> {

    List<MentorOffering> findByMentorProfileIdOrderByCreatedAtDesc(UUID mentorProfileId);

    List<MentorOffering> findByMentorProfileIdAndStatusOrderByCreatedAtDesc(UUID mentorProfileId, CourseStatus status);
}

