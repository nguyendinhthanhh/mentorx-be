package com.mentorx.api.feature.mentor.repository;

import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.feature.mentor.entity.MentorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MentorProfileRepository extends JpaRepository<MentorProfile, UUID> {

    Optional<MentorProfile> findByUserId(UUID userId);

    List<MentorProfile> findByStatus(MentorStatus status);

    Page<MentorProfile> findByStatus(MentorStatus status, Pageable pageable);

    @Query("SELECT mp FROM MentorProfile mp WHERE mp.status = :status " +
           "AND mp.isAvailableForJobs = true " +
           "ORDER BY mp.createdAt DESC")
    Page<MentorProfile> findAvailableMentorsForJobs(@Param("status") MentorStatus status, Pageable pageable);

    @Query("SELECT mp FROM MentorProfile mp WHERE mp.status = :status " +
           "AND mp.isAvailableForCourses = true " +
           "ORDER BY mp.createdAt DESC")
    Page<MentorProfile> findAvailableMentorsForCourses(@Param("status") MentorStatus status, Pageable pageable);

    @Query("SELECT mp FROM MentorProfile mp " +
           "JOIN mp.mentorSkills ms " +
           "WHERE mp.status = 'APPROVED' " +
           "AND LOWER(ms.skillName) LIKE LOWER(CONCAT('%', :skillName, '%')) " +
           "ORDER BY ms.proficiencyLevel DESC")
    List<MentorProfile> findBySkillName(@Param("skillName") String skillName);

    @Query("SELECT mp FROM MentorProfile mp WHERE mp.status = 'APPROVED' " +
           "AND mp.hourlyRate BETWEEN :minRate AND :maxRate " +
           "ORDER BY mp.hourlyRate ASC")
    List<MentorProfile> findByHourlyRateRange(@Param("minRate") BigDecimal minRate, 
                                             @Param("maxRate") BigDecimal maxRate);

    @Query("SELECT mp FROM MentorProfile mp WHERE mp.status = 'APPROVED' " +
           "AND (:skillName IS NULL OR EXISTS (SELECT 1 FROM mp.mentorSkills ms WHERE LOWER(ms.skillName) LIKE LOWER(CONCAT('%', :skillName, '%')))) " +
           "AND (:minRate IS NULL OR mp.hourlyRate >= :minRate) " +
           "AND (:maxRate IS NULL OR mp.hourlyRate <= :maxRate) " +
           "AND (:minExperience IS NULL OR mp.yearsOfExperience >= :minExperience) " +
           "AND (:availableForJobs IS NULL OR mp.isAvailableForJobs = :availableForJobs) " +
           "ORDER BY mp.createdAt DESC")
    Page<MentorProfile> findMentorsWithFilters(@Param("skillName") String skillName,
                                              @Param("minRate") BigDecimal minRate,
                                              @Param("maxRate") BigDecimal maxRate,
                                              @Param("minExperience") Integer minExperience,
                                              @Param("availableForJobs") Boolean availableForJobs,
                                              Pageable pageable);

    @Query("SELECT COUNT(mp) FROM MentorProfile mp WHERE mp.status = :status")
    long countByStatus(@Param("status") MentorStatus status);

    @Query("SELECT mp FROM MentorProfile mp " +
           "WHERE mp.status = 'APPROVED' " +
           "AND (LOWER(mp.user.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(mp.professionalTitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(mp.expertiseSummary) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY mp.createdAt DESC")
    List<MentorProfile> findByFullTextSearch(@Param("searchTerm") String searchTerm);

    @Query("SELECT mp FROM MentorProfile mp " +
           "WHERE mp.status = 'APPROVED' " +
           "AND mp.responseTimeHours <= :maxResponseTime " +
           "ORDER BY mp.responseTimeHours ASC")
    List<MentorProfile> findByMaxResponseTime(@Param("maxResponseTime") Integer maxResponseTime);
}