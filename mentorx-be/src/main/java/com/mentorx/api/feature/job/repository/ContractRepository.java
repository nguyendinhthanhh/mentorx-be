package com.mentorx.api.feature.job.repository;

import com.mentorx.api.feature.job.entity.Contract;
import com.mentorx.api.feature.job.enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {
    Page<Contract> findByJobId(UUID jobId, Pageable pageable);
    Page<Contract> findByClientId(UUID clientId, Pageable pageable);
    Page<Contract> findByMentorId(UUID mentorId, Pageable pageable);
    Page<Contract> findByStatus(ContractStatus status, Pageable pageable);
}
