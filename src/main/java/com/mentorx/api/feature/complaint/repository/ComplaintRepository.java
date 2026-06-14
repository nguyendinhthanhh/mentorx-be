package com.mentorx.api.feature.complaint.repository;

import com.mentorx.api.feature.complaint.entity.Complaint;
import com.mentorx.api.feature.complaint.enums.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {
    Page<Complaint> findByComplainantIdOrRespondentId(UUID complainantId, UUID respondentId, Pageable pageable);
    Page<Complaint> findByStatus(ComplaintStatus status, Pageable pageable);
    List<Complaint> findByStatusAndResponseDeadlineBefore(ComplaintStatus status, LocalDateTime cutoff);
}
