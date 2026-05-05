package com.mentorx.api.feature.chat.repository;

import com.mentorx.api.feature.chat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    Page<Message> findByChatRoomIdOrderBySentAtDesc(UUID chatRoomId, Pageable pageable);
}
