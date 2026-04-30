package com.mentorx.api.feature.chat.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing membership of a user in a chat room
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "chat_room_members", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"chat_room_id", "user_id"}),
       indexes = {
    @Index(name = "idx_chat_member_room_id", columnList = "chat_room_id"),
    @Index(name = "idx_chat_member_user_id", columnList = "user_id"),
    @Index(name = "idx_chat_member_active", columnList = "is_active"),
    @Index(name = "idx_chat_member_role", columnList = "member_role"),
    @Index(name = "idx_chat_member_joined", columnList = "joined_at DESC")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomMember extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Role of the member in the chat room
     */
    @NotNull
    @Size(max = 20)
    @Column(name = "member_role", nullable = false, length = 20)
    private String memberRole = "MEMBER"; // OWNER, ADMIN, MODERATOR, MEMBER

    /**
     * Whether this member is currently active in the room
     */
    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive = true;

    /**
     * When the user joined this chat room
     */
    @NotNull
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    /**
     * When the user left this chat room (if applicable)
     */
    @Column(name = "left_at")
    private LocalDateTime leftAt;

    /**
     * Last time the user was seen in this room
     */
    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    /**
     * ID of the last message the user has read
     */
    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    /**
     * When the user last read messages in this room
     */
    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    /**
     * Number of unread messages for this user
     */
    @Column(name = "unread_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer unreadCount = 0;

    /**
     * Whether the user has muted notifications for this room
     */
    @Column(name = "is_muted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isMuted = false;

    /**
     * When notifications were muted (if applicable)
     */
    @Column(name = "muted_at")
    private LocalDateTime mutedAt;

    /**
     * Until when notifications are muted (null = indefinitely)
     */
    @Column(name = "muted_until")
    private LocalDateTime mutedUntil;

    /**
     * Whether the user is currently typing
     */
    @Column(name = "is_typing", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isTyping = false;

    /**
     * When the user started typing
     */
    @Column(name = "typing_started_at")
    private LocalDateTime typingStartedAt;

    /**
     * Whether the user is currently online
     */
    @Column(name = "is_online", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isOnline = false;

    /**
     * Custom nickname in this room
     */
    @Size(max = 100)
    @Column(name = "nickname", length = 100)
    private String nickname;

    /**
     * User's status in this room
     */
    @Size(max = 200)
    @Column(name = "status_message", length = 200)
    private String statusMessage;

    /**
     * Whether the user can send messages
     */
    @Column(name = "can_send_messages", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean canSendMessages = true;

    /**
     * Whether the user can share files
     */
    @Column(name = "can_share_files", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean canShareFiles = true;

    /**
     * Whether the user can invite others
     */
    @Column(name = "can_invite_members", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean canInviteMembers = false;

    /**
     * Who invited this user to the room
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_user_id")
    private User invitedByUser;

    /**
     * Reason for leaving (if applicable)
     */
    @Size(max = 200)
    @Column(name = "leave_reason", length = 200)
    private String leaveReason;

    @PrePersist
    protected void onCreate() {
        if (this.joinedAt == null) {
            this.joinedAt = LocalDateTime.now();
        }
        this.lastSeenAt = LocalDateTime.now();
    }

    /**
     * Checks if the member has left the room
     */
    public boolean hasLeft() {
        return this.leftAt != null || !this.isActive;
    }

    /**
     * Checks if the member is an admin or owner
     */
    public boolean isAdminOrOwner() {
        return "OWNER".equals(this.memberRole) || "ADMIN".equals(this.memberRole);
    }

    /**
     * Checks if the member can moderate the room
     */
    public boolean canModerate() {
        return "OWNER".equals(this.memberRole) || 
               "ADMIN".equals(this.memberRole) || 
               "MODERATOR".equals(this.memberRole);
    }

    /**
     * Checks if notifications are currently muted
     */
    public boolean isCurrentlyMuted() {
        if (!this.isMuted) {
            return false;
        }
        
        if (this.mutedUntil == null) {
            return true; // Muted indefinitely
        }
        
        return LocalDateTime.now().isBefore(this.mutedUntil);
    }

    /**
     * Updates the last seen timestamp
     */
    public void updateLastSeen() {
        this.lastSeenAt = LocalDateTime.now();
        this.isOnline = true;
    }

    /**
     * Marks messages as read up to a certain message ID
     */
    public void markAsRead(Long messageId) {
        this.lastReadMessageId = messageId;
        this.lastReadAt = LocalDateTime.now();
        this.unreadCount = 0;
    }
}