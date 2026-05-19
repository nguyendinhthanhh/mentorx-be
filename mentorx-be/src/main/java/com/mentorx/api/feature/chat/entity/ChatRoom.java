package com.mentorx.api.feature.chat.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.chat.enums.ChatRoomType;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Entity representing a chat room for messaging between users
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "chat_rooms", indexes = {
    @Index(name = "idx_chat_room_type", columnList = "room_type"),
    @Index(name = "idx_chat_room_created_by", columnList = "created_by_user_id"),
    @Index(name = "idx_chat_room_active", columnList = "is_active"),
    @Index(name = "idx_chat_room_last_activity", columnList = "last_activity_at DESC"),
    @Index(name = "idx_chat_room_reference", columnList = "reference_type, reference_id")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom extends BaseEntity {

    /**
     * Type of chat room
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 30)
    private ChatRoomType roomType;

    /**
     * Display name of the chat room
     */
    @Size(max = 200)
    @Column(name = "room_name", length = 200)
    private String roomName;

    /**
     * Description of the chat room
     */
    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    /**
     * User who created this chat room
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    /**
     * Whether this chat room is currently active
     */
    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive = true;

    /**
     * Whether this is a private chat room
     */
    @Column(name = "is_private", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isPrivate = true;

    /**
     * Maximum number of members allowed
     */
    @Column(name = "max_members", columnDefinition = "INTEGER DEFAULT 2")
    private Integer maxMembers = 2;

    /**
     * Current number of members
     */
    @Column(name = "member_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer memberCount = 0;

    /**
     * Reference to related entity (job_id, course_id, contract_id, etc.)
     */
    @Column(name = "reference_id")
    private java.util.UUID referenceId;

    /**
     * Type of the referenced entity
     */
    @Size(max = 50)
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    /**
     * Last activity timestamp in this room
     */
    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    /**
     * ID of the last message sent
     */
    @Column(name = "last_message_id")
    private java.util.UUID lastMessageId;

    /**
     * Preview of the last message
     */
    @Size(max = 200)
    @Column(name = "last_message_preview", length = 200)
    private String lastMessagePreview;

    /**
     * When the last message was sent
     */
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    /**
     * ID of user who sent the last message
     */
    @Column(name = "last_message_sender_id")
    private java.util.UUID lastMessageSenderId;

    /**
     * Total number of messages in this room
     */
    @Column(name = "message_count", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long messageCount = 0L;

    /**
     * Room settings as JSON
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "room_settings", columnDefinition = "jsonb")
    private Map<String, Object> roomSettings;

    /**
     * Whether message history is enabled
     */
    @Column(name = "history_enabled", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean historyEnabled = true;

    /**
     * Whether file sharing is enabled
     */
    @Column(name = "file_sharing_enabled", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean fileSharingEnabled = true;

    /**
     * Whether voice messages are enabled
     */
    @Column(name = "voice_enabled", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean voiceEnabled = true;

    /**
     * Whether video messages are enabled
     */
    @Column(name = "video_enabled", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean videoEnabled = true;

    /**
     * Room avatar/image URL
     */
    @Size(max = 500)
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /**
     * When this room was archived (if applicable)
     */
    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    /**
     * Reason for archiving
     */
    @Size(max = 200)
    @Column(name = "archive_reason", length = 200)
    private String archiveReason;

    /**
     * Chat room members
     */
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatRoomMember> members = new ArrayList<>();

    /**
     * Messages in this chat room
     */
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> messages = new ArrayList<>();

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastActivityAt = LocalDateTime.now();
    }

    /**
     * Checks if the room is archived
     */
    public boolean isArchived() {
        return this.archivedAt != null;
    }

    /**
     * Checks if the room is full
     */
    public boolean isFull() {
        return this.memberCount != null && this.maxMembers != null && 
               this.memberCount >= this.maxMembers;
    }

    /**
     * Generates a display name for the room if not set
     */
    public String getDisplayName() {
        if (roomName != null && !roomName.trim().isEmpty()) {
            return roomName;
        }
        
        // Generate name based on room type and members
        switch (roomType) {
            case DIRECT_MESSAGE:
                return "Direct Message";
            case PROJECT_GROUP:
                return "Project Chat";
            case COURSE_DISCUSSION:
                return "Course Discussion";
            case QUICK_SUPPORT:
                return "Quick Support";
            case MENTORING_SESSION:
                return "Mentoring Session";
            default:
                return "Chat Room";
        }
    }
}