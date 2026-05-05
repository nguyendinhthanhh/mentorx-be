package com.mentorx.api.feature.chat.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.chat.dto.request.ChatRoomCreateRequest;
import com.mentorx.api.feature.chat.dto.request.MessageSendRequest;
import com.mentorx.api.feature.chat.dto.response.ChatRoomResponse;
import com.mentorx.api.feature.chat.dto.response.MessageResponse;
import com.mentorx.api.feature.chat.entity.ChatRoom;
import com.mentorx.api.feature.chat.entity.ChatRoomMember;
import com.mentorx.api.feature.chat.entity.Message;
import com.mentorx.api.feature.chat.repository.ChatRoomMemberRepository;
import com.mentorx.api.feature.chat.repository.ChatRoomRepository;
import com.mentorx.api.feature.chat.repository.MessageRepository;
import com.mentorx.api.feature.chat.service.ChatService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ChatRoomResponse createRoom(ChatRoomCreateRequest request) {
        User creator = userRepository.findById(request.createdByUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ChatRoom room = new ChatRoom();
        room.setRoomType(request.roomType());
        room.setRoomName(request.roomName());
        room.setDescription(request.description());
        room.setCreatedByUser(creator);
        room.setIsPrivate(request.isPrivate() != null ? request.isPrivate() : true);
        room.setMaxMembers(request.maxMembers() != null ? request.maxMembers() : 2);
        room.setReferenceId(request.referenceId());
        room.setReferenceType(request.referenceType());
        room.setLastActivityAt(LocalDateTime.now());

        ChatRoom savedRoom = chatRoomRepository.save(room);

        // Add creator as member
        addMemberToRoom(savedRoom, creator, "OWNER", null);
        
        // Add other members
        if (request.memberIds() != null) {
            for (UUID memberId : request.memberIds()) {
                if (!memberId.equals(creator.getId())) {
                    User member = userRepository.findById(memberId)
                            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                    addMemberToRoom(savedRoom, member, "MEMBER", creator);
                }
            }
        }

        savedRoom.setMemberCount(savedRoom.getMembers().size());
        return toRoomResponse(chatRoomRepository.save(savedRoom));
    }

    @Override
    public ChatRoomResponse getRoomById(UUID roomId) {
        return toRoomResponse(findRoom(roomId));
    }

    @Override
    public Page<ChatRoomResponse> getUserRooms(UUID userId, Pageable pageable) {
        return chatRoomRepository.findActiveRoomsByUserId(userId, pageable)
                .map(this::toRoomResponse);
    }

    @Override
    @Transactional
    public ChatRoomResponse addMember(UUID roomId, UUID userId, UUID addedByUserId) {
        ChatRoom room = findRoom(roomId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        User addedBy = userRepository.findById(addedByUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (room.isFull()) {
            throw new AppException(ErrorCode.BAD_REQUEST); // Or ROOM_FULL
        }

        addMemberToRoom(room, user, "MEMBER", addedBy);
        room.setMemberCount(room.getMembers().size());
        return toRoomResponse(chatRoomRepository.save(room));
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(MessageSendRequest request) {
        ChatRoom room = findRoom(request.chatRoomId());
        User sender = userRepository.findById(request.senderId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Ensure sender is a member
        ChatRoomMember member = chatRoomMemberRepository.findByChatRoomIdAndUserId(room.getId(), sender.getId())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED_CHAT_ACCESS));

        if (!member.getIsActive() || !member.getCanSendMessages()) {
            throw new AppException(ErrorCode.UNAUTHORIZED_CHAT_ACCESS);
        }

        Message replyTo = null;
        if (request.replyToMessageId() != null) {
            replyTo = messageRepository.findById(request.replyToMessageId()).orElse(null);
        }

        Message message = new Message();
        message.setChatRoom(room);
        message.setSender(sender);
        message.setMessageType(request.messageType());
        message.setContent(request.content());
        message.setReplyToMessage(replyTo);
        message.setAttachmentUrl(request.attachmentUrl());
        message.setAttachmentFilename(request.attachmentFilename());
        message.setAttachmentMimeType(request.attachmentMimeType());
        message.setAttachmentSize(request.attachmentSize());
        message.setMetadata(request.metadata());
        
        Message savedMessage = messageRepository.save(message);

        // Update room
        room.setLastMessageId(savedMessage.getId());
        room.setLastMessagePreview(savedMessage.getDisplayContent());
        room.setLastMessageAt(savedMessage.getSentAt());
        room.setLastMessageSenderId(sender.getId());
        room.setMessageCount(room.getMessageCount() + 1);
        room.setLastActivityAt(LocalDateTime.now());
        chatRoomRepository.save(room);

        // Update sender's last read
        member.markAsRead(savedMessage.getId());
        chatRoomMemberRepository.save(member);

        return toMessageResponse(savedMessage);
    }

    @Override
    public Page<MessageResponse> getRoomMessages(UUID roomId, Pageable pageable) {
        return messageRepository.findByChatRoomIdOrderBySentAtDesc(roomId, pageable)
                .map(this::toMessageResponse);
    }

    @Override
    @Transactional
    public MessageResponse markMessageAsRead(UUID messageId, UUID userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));
        
        ChatRoomMember member = chatRoomMemberRepository.findByChatRoomIdAndUserId(message.getChatRoom().getId(), userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED_CHAT_ACCESS));
                
        member.markAsRead(message.getId());
        chatRoomMemberRepository.save(member);
        
        message.setReadCount(message.getReadCount() + 1);
        return toMessageResponse(messageRepository.save(message));
    }

    private ChatRoom findRoom(UUID roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    private void addMemberToRoom(ChatRoom room, User user, String role, User invitedBy) {
        boolean exists = chatRoomMemberRepository.findByChatRoomIdAndUserId(room.getId(), user.getId()).isPresent();
        if (!exists) {
            ChatRoomMember member = new ChatRoomMember();
            member.setChatRoom(room);
            member.setUser(user);
            member.setMemberRole(role);
            member.setJoinedAt(LocalDateTime.now());
            member.setInvitedByUser(invitedBy);
            room.getMembers().add(member);
        }
    }

    private ChatRoomResponse toRoomResponse(ChatRoom room) {
        return new ChatRoomResponse(
                room.getId(),
                room.getRoomType(),
                room.getDisplayName(),
                room.getDescription(),
                room.getCreatedByUser().getId(),
                room.getIsActive(),
                room.getIsPrivate(),
                room.getMaxMembers(),
                room.getMemberCount(),
                room.getReferenceId(),
                room.getReferenceType(),
                room.getLastActivityAt(),
                room.getLastMessageId(),
                room.getLastMessagePreview(),
                room.getLastMessageAt(),
                room.getLastMessageSenderId(),
                room.getMessageCount(),
                room.getRoomSettings(),
                room.getAvatarUrl(),
                room.isArchived(),
                room.getArchivedAt(),
                room.getCreatedAt(),
                room.getUpdatedAt()
        );
    }

    private MessageResponse toMessageResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getChatRoom().getId(),
                message.getSender().getId(),
                message.getSender().getFullName(),
                message.getSender().getAvatarUrl(),
                message.getMessageType(),
                message.getContent(),
                message.getSentAt(),
                message.getReplyToMessage() != null ? message.getReplyToMessage().getId() : null,
                message.getIsEdited(),
                message.getEditedAt(),
                message.getIsDeleted(),
                message.getAttachmentUrl(),
                message.getAttachmentFilename(),
                message.getAttachmentMimeType(),
                message.getAttachmentSize(),
                message.getMetadata(),
                message.getReadCount(),
                message.getIsSystemMessage(),
                message.getCreatedAt(),
                message.getUpdatedAt()
        );
    }
}
