package com.mentorx.api.feature.moderation.repository;

import com.mentorx.api.feature.moderation.entity.Dispute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, UUID> {
    Page<Dispute> findByInitiatorIdOrRespondentId(UUID initiatorId, UUID respondentId, Pageable pageable);
}
