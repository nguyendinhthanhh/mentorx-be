package com.mentorx.api.feature.chat.repository;

import com.mentorx.api.feature.chat.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, UUID> {

    @EntityGraph(attributePaths = {"user"})
    List<ChatRoomMember> findByChatRoomIdOrderByJoinedAtAsc(UUID chatRoomId);

    Optional<ChatRoomMember> findByChatRoomIdAndUserId(UUID chatRoomId, UUID userId);
}
