package com.mentorx.api.feature.mentor.repository;

import com.mentorx.api.feature.mentor.entity.MentorBlockedDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MentorBlockedDateRepository extends JpaRepository<MentorBlockedDate, UUID> {

    List<MentorBlockedDate> findByMentorProfileIdOrderByBlockedDateAsc(UUID mentorProfileId);

    List<MentorBlockedDate> findByMentorProfileIdAndBlockedDateBetweenOrderByBlockedDateAsc(
            UUID mentorProfileId, LocalDate startDate, LocalDate endDate);

    boolean existsByMentorProfileIdAndBlockedDate(UUID mentorProfileId, LocalDate blockedDate);
}
