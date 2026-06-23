package com.mentorx.api.feature.job.repository;

import com.mentorx.api.common.enums.JobStatus;
import com.mentorx.api.common.enums.JobType;
import com.mentorx.api.feature.job.entity.Job;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

    String JOBS_COLUMNS =
        "j.id, j.created_at, j.updated_at, j.attachment_url, j.availability_end_time, " +
        "j.availability_expectation, j.availability_start_time, j.budget_max_mxc, j.budget_min_mxc, " +
        "j.budget_type, j.category_id, j.closed_at, j.communication_preference, j.current_level, " +
        "j.custom_category_name, j.deadline_at, j.deleted_at, j.description, j.estimated_hours, " +
        "j.expected_sessions, j.expected_weeks, j.experience_level, j.hourly_rate_mxc, j.is_featured, " +
        "j.job_type, j.learning_goals, j.preferred_language, j.proposal_count, j.published_at, " +
        "j.start_date, j.status, j.status_reason, j.success_criteria, j.timezone, j.title, " +
        "j.view_count, j.visibility, j.client_id";

    String JOBS_FROM = "FROM jobs j LEFT JOIN users u ON u.id = j.client_id";

    String JOBS_WHERE =
        "j.deleted_at IS NULL " +
        "AND (:status IS NULL OR j.status = CAST(:status AS varchar)) " +
        "AND (:jobType IS NULL OR j.job_type = CAST(:jobType AS varchar)) " +
        "AND (:categoryId IS NULL OR j.category_id = :categoryId) " +
        "AND (:skillKeyword IS NULL OR EXISTS (SELECT 1 FROM job_required_skills sk WHERE sk.job_id = j.id AND LOWER(sk.skill) LIKE LOWER(CONCAT('%', :skillKeyword, '%')))) " +
        "AND (:budgetMin IS NULL OR j.budget_max_mxc >= :budgetMin) " +
        "AND (:budgetMax IS NULL OR j.budget_min_mxc <= :budgetMax) " +
        "AND (:budgetType IS NULL OR j.budget_type = CAST(:budgetType AS varchar))";

    String KEYWORD_CONDITION =
        "AND (:keyword IS NULL OR to_tsvector('simple', j.title) @@ plainto_tsquery('simple', :keyword) " +
        "OR j.title ILIKE CONCAT('%', :keyword, '%'))";

    String KEYWORD_CONDITION_REQUIRED =
        "AND (to_tsvector('simple', j.title) @@ plainto_tsquery('simple', :keyword) " +
        "OR j.title ILIKE CONCAT('%', :keyword, '%'))";

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT j FROM Job j WHERE j.id = :jobId")
    java.util.Optional<Job> findByIdForUpdate(@Param("jobId") UUID jobId);

    Page<Job> findByClientId(UUID clientId, Pageable pageable);

    Page<Job> findByStatus(JobStatus status, Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.deletedAt IS NULL " +
           "AND (:status IS NULL OR j.status = :status) " +
           "AND (:jobType IS NULL OR j.jobType = :jobType) " +
           "AND (:categoryId IS NULL OR j.categoryId = :categoryId) " +
           "ORDER BY j.publishedAt DESC, j.createdAt DESC")
    Page<Job> findAllWithFilters(@Param("status") JobStatus status,
                                 @Param("jobType") JobType jobType,
                                 @Param("categoryId") Integer categoryId,
                                 Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.deletedAt IS NULL AND j.status = 'OPEN' ORDER BY j.publishedAt DESC, j.createdAt DESC")
    Page<Job> findOpen(Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.deletedAt IS NULL AND j.status = 'OPEN' " +
           "AND (:jobType IS NULL OR j.jobType = :jobType) " +
           "AND (:categoryId IS NULL OR j.categoryId = :categoryId) " +
           "ORDER BY j.publishedAt DESC, j.createdAt DESC")
    Page<Job> findOpenWithFilters(@Param("jobType") JobType jobType, @Param("categoryId") Integer categoryId, Pageable pageable);

    @Query(
            value = "SELECT * FROM jobs j " +
                    "WHERE j.deleted_at IS NULL " +
                    "AND j.status = 'OPEN' " +
                    "AND (:jobType IS NULL OR j.job_type = CAST(:jobType AS varchar)) " +
                    "AND (:categoryId IS NULL OR j.category_id = :categoryId) " +
                    "AND (:skillKeyword IS NULL OR EXISTS (SELECT 1 FROM job_required_skills sk WHERE sk.job_id = j.id AND LOWER(sk.skill) LIKE LOWER(CONCAT('%', :skillKeyword, '%')))) " +
                    "ORDER BY j.published_at DESC NULLS LAST, j.created_at DESC",
            countQuery = "SELECT COUNT(*) FROM jobs j " +
                    "WHERE j.deleted_at IS NULL " +
                    "AND j.status = 'OPEN' " +
                    "AND (:jobType IS NULL OR j.job_type = CAST(:jobType AS varchar)) " +
                    "AND (:categoryId IS NULL OR j.category_id = :categoryId) " +
                    "AND (:skillKeyword IS NULL OR EXISTS (SELECT 1 FROM job_required_skills sk WHERE sk.job_id = j.id AND LOWER(sk.skill) LIKE LOWER(CONCAT('%', :skillKeyword, '%'))))",
            nativeQuery = true
    )
    Page<Job> findOpenWithAdvancedFilters(@Param("jobType") String jobType,
                                          @Param("categoryId") Integer categoryId,
                                          @Param("skillKeyword") String skillKeyword,
                                          Pageable pageable);

    @Query(
            value = "SELECT * FROM jobs j " +
                    "WHERE j.deleted_at IS NULL " +
                    "AND (:status IS NULL OR j.status = CAST(:status AS varchar)) " +
                    "AND (:jobType IS NULL OR j.job_type = CAST(:jobType AS varchar)) " +
                    "AND (:categoryId IS NULL OR j.category_id = :categoryId) " +
                    "AND (:skillKeyword IS NULL OR EXISTS (SELECT 1 FROM job_required_skills sk WHERE sk.job_id = j.id AND LOWER(sk.skill) LIKE LOWER(CONCAT('%', :skillKeyword, '%')))) " +
                    "ORDER BY j.published_at DESC NULLS LAST, j.created_at DESC",
            countQuery = "SELECT COUNT(*) FROM jobs j " +
                    "WHERE j.deleted_at IS NULL " +
                    "AND (:status IS NULL OR j.status = CAST(:status AS varchar)) " +
                    "AND (:jobType IS NULL OR j.job_type = CAST(:jobType AS varchar)) " +
                    "AND (:categoryId IS NULL OR j.category_id = :categoryId) " +
                    "AND (:skillKeyword IS NULL OR EXISTS (SELECT 1 FROM job_required_skills sk WHERE sk.job_id = j.id AND LOWER(sk.skill) LIKE LOWER(CONCAT('%', :skillKeyword, '%'))))",
            nativeQuery = true
    )
    Page<Job> findAllWithAdvancedFilters(@Param("status") String status,
                                         @Param("jobType") String jobType,
                                         @Param("categoryId") Integer categoryId,
                                         @Param("skillKeyword") String skillKeyword,
                                         Pageable pageable);

    @Query(
            value = "SELECT " + JOBS_COLUMNS + ", u.full_name AS client_name, " +
                    "CAST(CASE WHEN :keyword IS NOT NULL THEN ts_rank(to_tsvector('simple', j.title), plainto_tsquery('simple', :keyword)) ELSE 0 END AS double precision) AS relevance_score " +
                    JOBS_FROM + " WHERE " + JOBS_WHERE + " " +
                    KEYWORD_CONDITION + " " +
                    "ORDER BY j.published_at DESC NULLS LAST, j.created_at DESC",
            countQuery = "SELECT COUNT(*) " + JOBS_FROM + " WHERE " + JOBS_WHERE + " " +
                    KEYWORD_CONDITION,
            nativeQuery = true
    )
    Page<Object[]> findOpenWithAllFilters(
            @Param("status") String status,
            @Param("jobType") String jobType,
            @Param("categoryId") Integer categoryId,
            @Param("skillKeyword") String skillKeyword,
            @Param("keyword") String keyword,
            @Param("budgetMin") BigDecimal budgetMin,
            @Param("budgetMax") BigDecimal budgetMax,
            @Param("budgetType") String budgetType,
            Pageable pageable);

    @Query(
            value = "SELECT " + JOBS_COLUMNS + ", u.full_name AS client_name, " +
                    "CAST(CASE WHEN :keyword IS NOT NULL THEN ts_rank(to_tsvector('simple', j.title), plainto_tsquery('simple', :keyword)) ELSE 0 END AS double precision) AS relevance_score " +
                    JOBS_FROM + " WHERE " + JOBS_WHERE + " " +
                    KEYWORD_CONDITION + " " +
                    "ORDER BY j.budget_max_mxc DESC NULLS LAST, j.published_at DESC",
            countQuery = "SELECT COUNT(*) " + JOBS_FROM + " WHERE " + JOBS_WHERE + " " +
                    KEYWORD_CONDITION,
            nativeQuery = true
    )
    Page<Object[]> findOpenBudgetDesc(
            @Param("status") String status,
            @Param("jobType") String jobType,
            @Param("categoryId") Integer categoryId,
            @Param("skillKeyword") String skillKeyword,
            @Param("keyword") String keyword,
            @Param("budgetMin") BigDecimal budgetMin,
            @Param("budgetMax") BigDecimal budgetMax,
            @Param("budgetType") String budgetType,
            Pageable pageable);

    @Query(
            value = "SELECT " + JOBS_COLUMNS + ", u.full_name AS client_name, " +
                    "CAST(CASE WHEN :keyword IS NOT NULL THEN ts_rank(to_tsvector('simple', j.title), plainto_tsquery('simple', :keyword)) ELSE 0 END AS double precision) AS relevance_score " +
                    JOBS_FROM + " WHERE " + JOBS_WHERE + " " +
                    KEYWORD_CONDITION + " " +
                    "ORDER BY j.budget_max_mxc ASC NULLS LAST, j.published_at DESC",
            countQuery = "SELECT COUNT(*) " + JOBS_FROM + " WHERE " + JOBS_WHERE + " " +
                    KEYWORD_CONDITION,
            nativeQuery = true
    )
    Page<Object[]> findOpenBudgetAsc(
            @Param("status") String status,
            @Param("jobType") String jobType,
            @Param("categoryId") Integer categoryId,
            @Param("skillKeyword") String skillKeyword,
            @Param("keyword") String keyword,
            @Param("budgetMin") BigDecimal budgetMin,
            @Param("budgetMax") BigDecimal budgetMax,
            @Param("budgetType") String budgetType,
            Pageable pageable);

    @Query(
            value = "SELECT " + JOBS_COLUMNS + ", u.full_name AS client_name, " +
                    "CAST(CASE WHEN :keyword IS NOT NULL THEN ts_rank(to_tsvector('simple', j.title), plainto_tsquery('simple', :keyword)) ELSE 0 END AS double precision) AS relevance_score " +
                    JOBS_FROM + " WHERE " + JOBS_WHERE + " " +
                    KEYWORD_CONDITION + " " +
                    "ORDER BY (j.view_count + j.proposal_count * 3) DESC, j.published_at DESC",
            countQuery = "SELECT COUNT(*) " + JOBS_FROM + " WHERE " + JOBS_WHERE + " " +
                    KEYWORD_CONDITION,
            nativeQuery = true
    )
    Page<Object[]> findOpenPopular(
            @Param("status") String status,
            @Param("jobType") String jobType,
            @Param("categoryId") Integer categoryId,
            @Param("skillKeyword") String skillKeyword,
            @Param("keyword") String keyword,
            @Param("budgetMin") BigDecimal budgetMin,
            @Param("budgetMax") BigDecimal budgetMax,
            @Param("budgetType") String budgetType,
            Pageable pageable);

    @Query(
            value = "SELECT " + JOBS_COLUMNS + ", u.full_name AS client_name, " +
"CAST(ts_rank(to_tsvector('simple', j.title), plainto_tsquery('simple', :keyword)) AS double precision) AS relevance_score " +
                     JOBS_FROM + " WHERE " + JOBS_WHERE + " " +
                     KEYWORD_CONDITION_REQUIRED + " " +
                     "ORDER BY relevance_score DESC, j.published_at DESC",
            countQuery = "SELECT COUNT(*) " + JOBS_FROM + " WHERE " + JOBS_WHERE + " " +
                    KEYWORD_CONDITION_REQUIRED,
            nativeQuery = true
    )
    Page<Object[]> findOpenRelevance(
            @Param("status") String status,
            @Param("jobType") String jobType,
            @Param("categoryId") Integer categoryId,
            @Param("skillKeyword") String skillKeyword,
            @Param("keyword") String keyword,
            @Param("budgetMin") BigDecimal budgetMin,
            @Param("budgetMax") BigDecimal budgetMax,
            @Param("budgetType") String budgetType,
            Pageable pageable);

    @Modifying
    @Query("UPDATE Job j SET j.status = 'EXPIRED' WHERE j.status = 'OPEN' AND j.deadlineAt IS NOT NULL AND j.deadlineAt < CURRENT_TIMESTAMP")
    int expirePastDeadlineJobs();

    // M12.2 H0: methods required by JobStatsServiceImpl.clientStats
    long countByClientIdAndDeletedAtIsNull(UUID clientId);
    long countByClientIdAndStatus(UUID clientId, JobStatus status);
}
