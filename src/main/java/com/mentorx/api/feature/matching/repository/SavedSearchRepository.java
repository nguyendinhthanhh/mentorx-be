package com.mentorx.api.feature.matching.repository;

import com.mentorx.api.feature.matching.entity.SavedSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavedSearchRepository extends JpaRepository<SavedSearch, UUID> {
    
    Page<SavedSearch> findByUserId(UUID userId, Pageable pageable);
    
    List<SavedSearch> findByUserId(UUID userId);
    
    Optional<SavedSearch> findByIdAndUserId(UUID id, UUID userId);
    
    boolean existsByUserIdAndName(UUID userId, String name);
    
    long countByUserId(UUID userId);
}
