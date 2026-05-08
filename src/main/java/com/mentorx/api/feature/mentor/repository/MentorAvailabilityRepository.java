package com.mentorx.api.feature.mentor.repository;

import com.mentorx.api.feature.mentor.entity.MentorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MentorAvailabilityRepository extends JpaRepository<MentorAvailability, UUID> {

    List<MentorAvailability> findByMentorProfileIdOrderByDayOfWeekAscStartTimeAsc(UUID mentorProfileId);

    List<MentorAvailability> findByMentorProfileIdAndIsActiveTrueOrderByDayOfWeekAscStartTimeAsc(UUID mentorProfileId);

    @Query("SELECT COUNT(a) > 0 FROM MentorAvailability a WHERE a.mentorProfileId = :mentorProfileId " +
            "AND a.dayOfWeek = :dayOfWeek " +
            "AND a.id != :excludeId " +
            "AND ((a.startTime < :endTime AND a.endTime > :startTime))")
    boolean existsOverlappingSlot(
            @Param("mentorProfileId") UUID mentorProfileId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") UUID excludeId
    );
}
