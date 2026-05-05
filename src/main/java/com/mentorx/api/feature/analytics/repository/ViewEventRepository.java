package com.mentorx.api.feature.analytics.repository;

import com.mentorx.api.feature.analytics.entity.ViewEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ViewEventRepository extends JpaRepository<ViewEvent, UUID> {
    long countByTargetTypeAndTargetId(String targetType, UUID targetId);
}
