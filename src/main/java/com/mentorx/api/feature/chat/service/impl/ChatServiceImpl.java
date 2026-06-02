package com.mentorx.api.feature.chat.service.impl;

import com.mentorx.api.common.security.MentorModeAccessService;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.chat.dto.request.ChatConversationResolveRequest;
import com.mentorx.api.feature.chat.dto.request.ChatRoomCreateRequest;
import com.mentorx.api.feature.chat.dto.response.ChatRoomMemberResponse;
import com.mentorx.api.feature.chat.dto.request.MessageSendRequest;
import com.mentorx.api.feature.chat.dto.response.ChatRoomResponse;
import com.mentorx.api.feature.chat.dto.response.MessageResponse;
import com.mentorx.api.feature.chat.entity.ChatRoom;
import com.mentorx.api.feature.chat.entity.ChatRoomMember;
import com.mentorx.api.feature.chat.entity.Message;
import com.mentorx.api.feature.chat.enums.ChatRoomType;
import com.mentorx.api.feature.chat.repository.ChatRoomMemberRepository;
import com.mentorx.api.feature.chat.repository.ChatRoomRepository;
import com.mentorx.api.feature.chat.repository.MessageRepository;
import com.mentorx.api.feature.chat.service.ChatService;
import com.mentorx.api.feature.job.entity.Contract;
import com.mentorx.api.feature.job.entity.Job;
import com.mentorx.api.feature.job.entity.Proposal;
import com.mentorx.api.feature.job.repository.ContractRepository;
import com.mentorx.api.feature.job.repository.JobRepository;
import com.mentorx.api.feature.job.repository.ProposalRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ProposalRepository proposalRepository;
    private final ContractRepository contractRepository;
    private final MentorModeAccessService mentorModeAccessService;

    @Override
    @Transactional
    public ChatRoomResponse createRoom(ChatRoomCreateRequest request) {
        requireCurrentUser(request.createdByUserId());
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
        return toRoomResponse(chatRoomRepository.save(savedRoom), creator.getId());
    }

    @Override
    @Transactional
    public ChatRoomResponse resolveConversation(ChatConversationResolveRequest request) {
        UUID currentUserId = mentorModeAccessService.getCurrentUserId();
        if (currentUserId.equals(request.recipientId())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "You cannot open a conversation with yourself.");
        }

        User recipient = userRepository.findById(request.recipientId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ResolvedConversationContext context = resolveContext(request.contextType(), request.contextId(), currentUserId, recipient.getId());

        return chatRoomRepository
                .findDirectRoomByReferenceAndMembers(context.referenceType(), context.referenceId(), currentUserId, recipient.getId())
                .or(() -> chatRoomRepository.findDirectRoomByReferenceAndMembers(context.referenceType(), context.referenceId(), recipient.getId(), currentUserId))
                .map(room -> toRoomResponse(room, currentUserId))
                .orElseGet(() -> createResolvedRoom(currentUserId, recipient.getId(), context.referenceType(), context.referenceId(), context.description()));
    }

    @Override
    public ChatRoomResponse getRoomById(UUID roomId, UUID userId) {
        requireCurrentUser(userId);
        ChatRoom room = findRoom(roomId);
        requireRoomMembership(roomId, userId);
        return toRoomResponse(room, userId);
    }

    @Override
    public Page<ChatRoomResponse> getUserRooms(UUID userId, Pageable pageable) {
        requireCurrentUser(userId);
        return chatRoomRepository.findActiveRoomsByUserId(userId, pageable)
                .map(room -> toRoomResponse(room, userId));
    }

    @Override
    @Transactional
    public ChatRoomResponse addMember(UUID roomId, UUID userId, UUID addedByUserId) {
        requireCurrentUser(addedByUserId);
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
        return toRoomResponse(chatRoomRepository.save(room), addedByUserId);
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(MessageSendRequest request) {
        UUID currentUserId = mentorModeAccessService.getCurrentUserId();
        ChatRoom room = findRoom(request.chatRoomId());
        User sender = userRepository.findById(currentUserId)
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

        // Update sender's last read and increment unread count for everyone else
        member.markAsRead(savedMessage.getId());
        member.updateLastSeen();
        chatRoomMemberRepository.save(member);

        List<ChatRoomMember> otherMembers = new ArrayList<>();
        for (ChatRoomMember roomMember : room.getMembers()) {
            if (roomMember.getUser().getId().equals(sender.getId()) || !Boolean.TRUE.equals(roomMember.getIsActive())) {
                continue;
            }
            roomMember.setUnreadCount((roomMember.getUnreadCount() == null ? 0 : roomMember.getUnreadCount()) + 1);
            otherMembers.add(roomMember);
        }
        if (!otherMembers.isEmpty()) {
            chatRoomMemberRepository.saveAll(otherMembers);
        }

        return toMessageResponse(savedMessage);
    }

    @Override
    public Page<MessageResponse> getRoomMessages(UUID roomId, Pageable pageable) {
        UUID currentUserId = mentorModeAccessService.getCurrentUserId();
        if (!mentorModeAccessService.isCurrentUserAdmin()) {
            requireRoomMembership(roomId, currentUserId);
        }
        return messageRepository.findByChatRoomIdOrderBySentAtAsc(roomId, pageable)
                .map(this::toMessageResponse);
    }

    @Override
    @Transactional
    public MessageResponse markMessageAsRead(UUID messageId, UUID userId) {
        requireCurrentUser(userId);
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));
        
        ChatRoomMember member = chatRoomMemberRepository.findByChatRoomIdAndUserId(message.getChatRoom().getId(), userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED_CHAT_ACCESS));
                
        member.markAsRead(message.getId());
        chatRoomMemberRepository.save(member);
        
        message.setReadCount(message.getReadCount() + 1);
        return toMessageResponse(messageRepository.save(message));
    }

    private ChatRoomResponse createResolvedRoom(
            UUID currentUserId,
            UUID recipientId,
            String referenceType,
            UUID referenceId,
            String description
    ) {
        ChatRoomCreateRequest createRequest = new ChatRoomCreateRequest(
                ChatRoomType.DIRECT_MESSAGE,
                null,
                description,
                currentUserId,
                true,
                2,
                referenceId,
                referenceType,
                List.of(currentUserId, recipientId)
        );
        return createRoom(createRequest);
    }

    private ChatRoom findRoom(UUID roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    private void requireCurrentUser(UUID userId) {
        if (!mentorModeAccessService.isCurrentUserAdmin() && !mentorModeAccessService.getCurrentUserId().equals(userId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "You cannot act on behalf of another user.");
        }
    }

    private void requireRoomMembership(UUID roomId, UUID userId) {
        chatRoomMemberRepository.findByChatRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED_CHAT_ACCESS));
    }

    private ResolvedConversationContext resolveContext(String rawContextType, UUID contextId, UUID currentUserId, UUID recipientId) {
        String contextType = rawContextType.trim().toUpperCase();
        return switch (contextType) {
            case "CONTRACT" -> resolveContractContext(contextId, currentUserId, recipientId);
            case "PROPOSAL" -> resolveProposalContext(contextId, currentUserId, recipientId);
            case "JOB" -> resolveJobContext(contextId, currentUserId, recipientId);
            default -> throw new AppException(ErrorCode.BAD_REQUEST, "Unsupported conversation context.");
        };
    }

    private ResolvedConversationContext resolveContractContext(UUID contractId, UUID currentUserId, UUID recipientId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        boolean currentIsClient = contract.getClient().getId().equals(currentUserId);
        boolean currentIsMentor = contract.getMentor().getId().equals(currentUserId);
        if (!currentIsClient && !currentIsMentor) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "You do not have access to this contract conversation.");
        }

        UUID expectedRecipient = currentIsClient ? contract.getMentor().getId() : contract.getClient().getId();
        if (!expectedRecipient.equals(recipientId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "This contract conversation can only be opened with the assigned counterparty.");
        }

        return new ResolvedConversationContext("CONTRACT", contractId, "Contract chat · " + contract.getJob().getTitle());
    }

    private ResolvedConversationContext resolveProposalContext(UUID proposalId, UUID currentUserId, UUID recipientId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new AppException(ErrorCode.PROPOSAL_NOT_FOUND));

        UUID clientId = proposal.getJob().getClient().getId();
        UUID mentorId = proposal.getMentor().getId();
        boolean currentIsClient = clientId.equals(currentUserId);
        boolean currentIsMentor = mentorId.equals(currentUserId);
        if (!currentIsClient && !currentIsMentor) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "You do not have access to this proposal conversation.");
        }

        UUID expectedRecipient = currentIsClient ? mentorId : clientId;
        if (!expectedRecipient.equals(recipientId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "This proposal conversation can only be opened with the matching client or mentor.");
        }

        return new ResolvedConversationContext("PROPOSAL", proposalId, "Proposal discussion · " + proposal.getJob().getTitle());
    }

    private ResolvedConversationContext resolveJobContext(UUID jobId, UUID currentUserId, UUID recipientId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        boolean currentIsClient = job.getClient().getId().equals(currentUserId);
        if (currentIsClient) {
            Proposal recipientProposal = proposalRepository.findByJobIdAndMentorId(jobId, recipientId)
                    .orElseThrow(() -> new AppException(ErrorCode.ACCESS_DENIED, "This mentor is not part of the selected job conversation."));
            return new ResolvedConversationContext("JOB", jobId, "Job conversation · " + recipientProposal.getJob().getTitle());
        }

        Proposal currentProposal = proposalRepository.findByJobIdAndMentorId(jobId, currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCESS_DENIED, "You do not have access to this job conversation."));
        if (!job.getClient().getId().equals(recipientId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Job conversations for mentors can only be opened with the job owner.");
        }

        return new ResolvedConversationContext("JOB", jobId, "Job conversation · " + currentProposal.getJob().getTitle());
    }

    private record ResolvedConversationContext(String referenceType, UUID referenceId, String description) {
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

    private ChatRoomResponse toRoomResponse(ChatRoom room, UUID currentUserId) {
        List<ChatRoomMember> roomMembers = chatRoomMemberRepository.findByChatRoomIdOrderByJoinedAtAsc(room.getId());

        List<ChatRoomMemberResponse> members = roomMembers.stream()
                .filter(member -> Boolean.TRUE.equals(member.getIsActive()))
                .map(member -> new ChatRoomMemberResponse(
                        member.getUser().getId(),
                        member.getUser().getFullName(),
                        member.getUser().getDisplayName(),
                        member.getUser().getAvatarUrl(),
                        member.getMemberRole(),
                        member.getIsOnline(),
                        member.getLastSeenAt()
                ))
                .collect(Collectors.toList());

        Integer unreadCount = roomMembers.stream()
                .filter(member -> currentUserId != null && member.getUser().getId().equals(currentUserId))
                .map(ChatRoomMember::getUnreadCount)
                .findFirst()
                .orElse(0);

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
                unreadCount,
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
                members,
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
