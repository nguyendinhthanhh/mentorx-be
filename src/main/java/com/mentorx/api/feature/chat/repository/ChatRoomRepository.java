package com.mentorx.api.feature.chat.repository;

import com.mentorx.api.feature.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

    @Override
    @EntityGraph(attributePaths = {"createdByUser", "members", "members.user"})
    java.util.Optional<ChatRoom> findById(UUID id);

    @EntityGraph(attributePaths = {"createdByUser", "members", "members.user"})
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.members m WHERE m.user.id = :userId AND cr.isActive = true ORDER BY cr.lastActivityAt DESC")
    Page<ChatRoom> findActiveRoomsByUserId(@Param("userId") UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"createdByUser", "members", "members.user"})
    @Query("""
            SELECT DISTINCT cr
            FROM ChatRoom cr
            JOIN cr.members firstMember
            JOIN cr.members secondMember
            WHERE cr.isActive = true
              AND cr.roomType = com.mentorx.api.feature.chat.enums.ChatRoomType.DIRECT_MESSAGE
              AND cr.referenceId = :referenceId
              AND UPPER(cr.referenceType) = UPPER(:referenceType)
              AND firstMember.user.id = :firstUserId
              AND secondMember.user.id = :secondUserId
              AND firstMember.isActive = true
              AND secondMember.isActive = true
            """)
    Optional<ChatRoom> findDirectRoomByReferenceAndMembers(
            @Param("referenceType") String referenceType,
            @Param("referenceId") UUID referenceId,
            @Param("firstUserId") UUID firstUserId,
            @Param("secondUserId") UUID secondUserId
    );
}
