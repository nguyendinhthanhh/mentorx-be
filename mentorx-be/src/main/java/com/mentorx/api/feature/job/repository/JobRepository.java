package com.mentorx.api.feature.job.repository;

import com.mentorx.api.common.enums.JobStatus;
import com.mentorx.api.common.enums.JobType;
import com.mentorx.api.feature.job.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

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

    @Query("SELECT j FROM Job j WHERE j.deletedAt IS NULL AND j.status = 'OPEN' " +
           "ORDER BY j.publishedAt DESC, j.createdAt DESC")
    Page<Job> findOpen(Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.deletedAt IS NULL AND j.status = 'OPEN' " +
           "AND (:jobType IS NULL OR j.jobType = :jobType) " +
           "AND (:categoryId IS NULL OR j.categoryId = :categoryId) " +
           "ORDER BY j.publishedAt DESC, j.createdAt DESC")
    Page<Job> findOpenWithFilters(@Param("jobType") JobType jobType, @Param("categoryId") Integer categoryId, Pageable pageable);
}
