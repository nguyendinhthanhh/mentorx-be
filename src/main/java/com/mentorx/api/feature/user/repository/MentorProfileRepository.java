package com.mentorx.api.feature.user.repository;

import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.common.enums.VerificationStatus;
import com.mentorx.api.feature.user.entity.MentorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MentorProfileRepository extends JpaRepository<MentorProfile, UUID> {

    Optional<MentorProfile> findByUserId(UUID userId);

    @Query("SELECT mp FROM MentorProfile mp WHERE mp.user.mentorStatus = :status")
    Page<MentorProfile> findByMentorStatus(@Param("status") MentorStatus status, Pageable pageable);

    @Query("SELECT mp FROM MentorProfile mp " +
           "WHERE mp.user.mentorStatus = 'APPROVED' " +
           "AND (:minRating IS NULL OR mp.averageRating >= :minRating) " +
           "AND (:maxHourlyRate IS NULL OR mp.hourlyRateMxc <= :maxHourlyRate) " +
           "AND (:availability IS NULL OR LOWER(mp.availability) = LOWER(:availability))")
    Page<MentorProfile> findApprovedWithFilters(@Param("minRating") BigDecimal minRating,
                                                @Param("maxHourlyRate") BigDecimal maxHourlyRate,
                                                @Param("availability") String availability,
                                                Pageable pageable);

    @Query("SELECT mp FROM MentorProfile mp WHERE mp.user.mentorStatus = 'APPROVED' AND mp.isFeatured = true")
    List<MentorProfile> findFeatured();

    @Query("SELECT mp FROM MentorProfile mp WHERE mp.user.mentorStatus = 'APPROVED'")
    Page<MentorProfile> findApproved(Pageable pageable);

    @Query("SELECT mp FROM MentorProfile mp WHERE mp.user.mentorStatus = 'APPROVED' " +
           "AND (LOWER(mp.headline) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(mp.user.fullName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(COALESCE(mp.user.bio, '')) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<MentorProfile> search(@Param("query") String query);

    @Query("SELECT COUNT(mp) FROM MentorProfile mp WHERE mp.user.mentorStatus = :status")
    long countByMentorStatus(@Param("status") MentorStatus status);

    @Query("SELECT mp FROM MentorProfile mp WHERE mp.identityStatus IN :statuses")
    Page<MentorProfile> findByIdentityStatuses(@Param("statuses") Collection<VerificationStatus> statuses, Pageable pageable);

    @Query("SELECT mp FROM MentorProfile mp WHERE mp.payoutStatus IN :statuses")
    Page<MentorProfile> findByPayoutStatuses(@Param("statuses") Collection<VerificationStatus> statuses, Pageable pageable);
}
