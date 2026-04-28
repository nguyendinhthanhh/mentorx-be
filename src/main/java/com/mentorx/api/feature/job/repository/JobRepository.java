package com.mentorx.api.feature.job.repository;

import com.mentorx.api.common.enums.JobStatus;
import com.mentorx.api.common.enums.JobType;
import com.mentorx.api.feature.job.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

    List<Job> findByClientId(UUID clientId);

    Page<Job> findByClientId(UUID clientId, Pageable pageable);

    List<Job> findByStatus(JobStatus status);

    Page<Job> findByStatus(JobStatus status, Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN' ORDER BY j.publishedAt DESC")
    Page<Job> findOpenJobs(Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN' AND j.isFeatured = true ORDER BY j.publishedAt DESC")
    Page<Job> findFeaturedJobs(Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN' AND j.isUrgent = true ORDER BY j.publishedAt DESC")
    Page<Job> findUrgentJobs(Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN' " +
           "AND (:jobType IS NULL OR j.jobType = :jobType) " +
           "AND (:category IS NULL OR LOWER(j.category) = LOWER(:category)) " +
           "AND (:minBudget IS NULL OR j.budgetAmount >= :minBudget) " +
           "AND (:maxBudget IS NULL OR j.budgetAmount <= :maxBudget) " +
           "AND (:isRemote IS NULL OR j.isRemote = :isRemote) " +
           "ORDER BY j.publishedAt DESC")
    Page<Job> findJobsWithFilters(@Param("jobType") JobType jobType,
                                 @Param("category") String category,
                                 @Param("minBudget") BigDecimal minBudget,
                                 @Param("maxBudget") BigDecimal maxBudget,
                                 @Param("isRemote") Boolean isRemote,
                                 Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN' " +
           "AND (LOWER(j.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(j.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(j.skillsRequired) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY j.publishedAt DESC")
    List<Job> findByFullTextSearch(@Param("searchTerm") String searchTerm);

    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN' " +
           "AND LOWER(j.skillsRequired) LIKE LOWER(CONCAT('%', :skill, '%')) " +
           "ORDER BY j.publishedAt DESC")
    List<Job> findBySkill(@Param("skill") String skill);

    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN' " +
           "AND j.budgetAmount BETWEEN :minBudget AND :maxBudget " +
           "ORDER BY j.budgetAmount ASC")
    List<Job> findByBudgetRange(@Param("minBudget") BigDecimal minBudget, 
                               @Param("maxBudget") BigDecimal maxBudget);

    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN' " +
           "AND j.deadline BETWEEN :startDate AND :endDate " +
           "ORDER BY j.deadline ASC")
    List<Job> findByDeadlineRange(@Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN' " +
           "AND LOWER(j.category) = LOWER(:category) " +
           "ORDER BY j.publishedAt DESC")
    Page<Job> findByCategory(@Param("category") String category, Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN' " +
           "AND LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')) " +
           "ORDER BY j.publishedAt DESC")
    List<Job> findByLocation(@Param("location") String location);

    @Query("SELECT COUNT(j) FROM Job j WHERE j.status = :status")
    long countByStatus(@Param("status") JobStatus status);

    @Query("SELECT COUNT(j) FROM Job j WHERE j.clientId = :clientId AND j.status = :status")
    long countByClientIdAndStatus(@Param("clientId") UUID clientId, @Param("status") JobStatus status);

    @Query("SELECT j FROM Job j WHERE j.publishedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY j.publishedAt DESC")
    List<Job> findJobsPublishedBetween(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT j.category, COUNT(j) as jobCount FROM Job j " +
           "WHERE j.status = 'OPEN' " +
           "GROUP BY j.category " +
           "ORDER BY jobCount DESC")
    List<Object[]> findJobCategoryStats();

    @Modifying
    @Query("UPDATE Job j SET j.viewCount = j.viewCount + 1 WHERE j.id = :jobId")
    void incrementViewCount(@Param("jobId") UUID jobId);

    @Modifying
    @Query("UPDATE Job j SET j.proposalCount = j.proposalCount + 1 WHERE j.id = :jobId")
    void incrementProposalCount(@Param("jobId") UUID jobId);

    @Modifying
    @Query("UPDATE Job j SET j.proposalCount = j.proposalCount - 1 WHERE j.id = :jobId AND j.proposalCount > 0")
    void decrementProposalCount(@Param("jobId") UUID jobId);

    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN' " +
           "AND j.deadline < :currentDate " +
           "ORDER BY j.deadline ASC")
    List<Job> findExpiredJobs(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN' " +
           "ORDER BY j.viewCount DESC, j.publishedAt DESC")
    Page<Job> findPopularJobs(Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.clientId = :clientId " +
           "ORDER BY j.createdAt DESC")
    Page<Job> findRecentJobsByClient(@Param("clientId") UUID clientId, Pageable pageable);
}