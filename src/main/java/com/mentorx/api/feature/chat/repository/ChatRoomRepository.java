package com.mentorx.api.feature.chat.repository;

import com.mentorx.api.feature.chat.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
    
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.members m WHERE m.user.id = :userId AND cr.isActive = true ORDER BY cr.lastActivityAt DESC")
    Page<ChatRoom> findActiveRoomsByUserId(@Param("userId") UUID userId, Pageable pageable);
}
