package com.mentorx.api.feature.chat.service;

import com.mentorx.api.feature.chat.dto.request.ChatRoomCreateRequest;
import com.mentorx.api.feature.chat.dto.request.MessageSendRequest;
import com.mentorx.api.feature.chat.dto.response.ChatRoomResponse;
import com.mentorx.api.feature.chat.dto.response.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ChatService {
    ChatRoomResponse createRoom(ChatRoomCreateRequest request);
    ChatRoomResponse getRoomById(UUID roomId, UUID userId);
    Page<ChatRoomResponse> getUserRooms(UUID userId, Pageable pageable);
    ChatRoomResponse addMember(UUID roomId, UUID userId, UUID addedByUserId);
    MessageResponse sendMessage(MessageSendRequest request);
    Page<MessageResponse> getRoomMessages(UUID roomId, Pageable pageable);
    MessageResponse markMessageAsRead(UUID messageId, UUID userId);
}
