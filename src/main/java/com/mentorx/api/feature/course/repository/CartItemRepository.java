package com.mentorx.api.feature.course.repository;

import com.mentorx.api.feature.course.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    
    List<CartItem> findByUserId(UUID userId);
    
    Optional<CartItem> findByUserIdAndCourseId(UUID userId, UUID courseId);
    
    boolean existsByUserIdAndCourseId(UUID userId, UUID courseId);
    
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.user.id = :userId")
    Long countByUserId(@Param("userId") UUID userId);
    
    void deleteByUserIdAndCourseId(UUID userId, UUID courseId);
    
    void deleteByUserId(UUID userId);
}
