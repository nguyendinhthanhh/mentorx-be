package com.mentorx.api.feature.chat.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.chat.dto.request.ChatRoomCreateRequest;
import com.mentorx.api.feature.chat.dto.request.ChatConversationResolveRequest;
import com.mentorx.api.feature.chat.dto.request.MessageSendRequest;
import com.mentorx.api.feature.chat.dto.request.UserBlockRequest;
import com.mentorx.api.feature.chat.dto.response.ChatRoomResponse;
import com.mentorx.api.feature.chat.dto.response.MessageResponse;
import com.mentorx.api.feature.chat.dto.response.UserBlockResponse;
import com.mentorx.api.feature.chat.service.ChatService;
import com.mentorx.api.feature.chat.service.UserBlockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserBlockService userBlockService;

    // --- Chat Room APIs ---

    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createRoom(@Valid @RequestBody ChatRoomCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(chatService.createRoom(request)));
    }

    @PostMapping("/rooms/resolve")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> resolveConversation(@Valid @RequestBody ChatConversationResolveRequest request) {
        return ResponseEntity.ok(ApiResponse.success(chatService.resolveConversation(request)));
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getRoomById(
            @PathVariable UUID roomId,
            @RequestParam UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(chatService.getRoomById(roomId, userId)));
    }

    @GetMapping("/users/{userId}/rooms")
    public ResponseEntity<ApiResponse<Page<ChatRoomResponse>>> getUserRooms(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(chatService.getUserRooms(userId, PageRequest.of(page, size))));
    }

    @PostMapping("/rooms/{roomId}/members")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> addMember(
            @PathVariable UUID roomId,
            @RequestParam UUID userId,
            @RequestParam UUID addedByUserId) {
        return ResponseEntity.ok(ApiResponse.success(chatService.addMember(roomId, userId, addedByUserId)));
    }

    // --- Message APIs ---

    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(@Valid @RequestBody MessageSendRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(chatService.sendMessage(request)));
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getRoomMessages(
            @PathVariable UUID roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(chatService.getRoomMessages(roomId, PageRequest.of(page, size))));
    }

    @PostMapping("/messages/{messageId}/read")
    public ResponseEntity<ApiResponse<MessageResponse>> markMessageAsRead(
            @PathVariable UUID messageId,
            @RequestParam UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(chatService.markMessageAsRead(messageId, userId)));
    }

    // --- User Block APIs ---

    @PostMapping("/blocks")
    public ResponseEntity<ApiResponse<UserBlockResponse>> blockUser(@Valid @RequestBody UserBlockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(userBlockService.blockUser(request)));
    }

    @PostMapping("/blocks/{blockId}/unblock")
    public ResponseEntity<ApiResponse<UserBlockResponse>> unblockUser(
            @PathVariable UUID blockId,
            @RequestParam(defaultValue = "") String reason) {
        return ResponseEntity.ok(ApiResponse.success(userBlockService.unblockUser(blockId, reason)));
    }
}
