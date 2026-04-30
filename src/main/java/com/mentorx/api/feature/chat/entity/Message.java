package com.mentorx.api.feature.chat.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.chat.enums.MessageType;
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
import java.util.Map;

/**
 * Entity representing a message in a chat room
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_message_chat_room_id", columnList = "chat_room_id"),
    @Index(name = "idx_message_sender_id", columnList = "sender_id"),
    @Index(name = "idx_message_type", columnList = "message_type"),
    @Index(name = "idx_message_sent_at", columnList = "sent_at DESC"),
    @Index(name = "idx_message_room_sent", columnList = "chat_room_id, sent_at DESC"),
    @Index(name = "idx_message_reply_to", columnList = "reply_to_message_id"),
    @Index(name = "idx_message_edited", columnList = "is_edited"),
    @Index(name = "idx_message_deleted", columnList = "is_deleted")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Message extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * Type of message
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 30)
    private MessageType messageType;

    /**
     * Content of the message
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * When the message was sent
     */
    @NotNull
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    /**
     * Message this is replying to (if any)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_message_id")
    private Message replyToMessage;

    /**
     * Whether this message has been edited
     */
    @Column(name = "is_edited", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isEdited = false;

    /**
     * When the message was last edited
     */
    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    /**
     * Whether this message has been deleted
     */
    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDeleted = false;

    /**
     * When the message was deleted
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Who deleted the message
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by_user_id")
    private User deletedByUser;

    /**
     * File attachment URL (if any)
     */
    @Size(max = 500)
    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    /**
     * Original filename of attachment
     */
    @Size(max = 255)
    @Column(name = "attachment_filename", length = 255)
    private String attachmentFilename;

    /**
     * MIME type of attachment
     */
    @Size(max = 100)
    @Column(name = "attachment_mime_type", length = 100)
    private String attachmentMimeType;

    /**
     * Size of attachment in bytes
     */
    @Column(name = "attachment_size")
    private Long attachmentSize;

    /**
     * Thumbnail URL for image/video attachments
     */
    @Size(max = 500)
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    /**
     * Duration in seconds for voice/video messages
     */
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    /**
     * Additional metadata as JSON
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * Number of users who have read this message
     */
    @Column(name = "read_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer readCount = 0;

    /**
     * Number of reactions to this message
     */
    @Column(name = "reaction_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer reactionCount = 0;

    /**
     * Whether this message is pinned in the room
     */
    @Column(name = "is_pinned", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isPinned = false;

    /**
     * When the message was pinned
     */
    @Column(name = "pinned_at")
    private LocalDateTime pinnedAt;

    /**
     * Who pinned the message
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pinned_by_user_id")
    private User pinnedByUser;

    /**
     * Whether this is a system message
     */
    @Column(name = "is_system_message", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isSystemMessage = false;

    /**
     * Priority level of the message
     */
    @Column(name = "priority_level", columnDefinition = "INTEGER DEFAULT 0")
    private Integer priorityLevel = 0;

    /**
     * Language of the message content
     */
    @Size(max = 10)
    @Column(name = "language", length = 10)
    private String language;

    /**
     * Whether this message requires translation
     */
    @Column(name = "needs_translation", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean needsTranslation = false;

    /**
     * Client message ID for deduplication
     */
    @Size(max = 100)
    @Column(name = "client_message_id", length = 100)
    private String clientMessageId;

    /**
     * Device type used to send the message
     */
    @Size(max = 20)
    @Column(name = "device_type", length = 20)
    private String deviceType;

    @PrePersist
    protected void onCreate() {
        if (this.sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }
        
        // Set system message flag based on message type
        if (this.messageType == MessageType.SYSTEM || 
            this.messageType == MessageType.SESSION_START || 
            this.messageType == MessageType.SESSION_END) {
            this.isSystemMessage = true;
        }
    }

    /**
     * Checks if this message can be edited
     */
    public boolean canBeEdited() {
        if (this.isDeleted || this.isSystemMessage) {
            return false;
        }
        
        // Allow editing within 15 minutes of sending
        return this.sentAt.isAfter(LocalDateTime.now().minusMinutes(15));
    }

    /**
     * Checks if this message can be deleted
     */
    public boolean canBeDeleted() {
        return !this.isDeleted;
    }

    /**
     * Checks if this message has attachments
     */
    public boolean hasAttachment() {
        return this.attachmentUrl != null && !this.attachmentUrl.trim().isEmpty();
    }

    /**
     * Gets display content (handles deleted messages)
     */
    public String getDisplayContent() {
        if (this.isDeleted) {
            return "[Message deleted]";
        }
        
        if (this.content == null || this.content.trim().isEmpty()) {
            switch (this.messageType) {
                case IMAGE:
                    return "[Image]";
                case FILE:
                    return "[File: " + (attachmentFilename != null ? attachmentFilename : "Unknown") + "]";
                case VOICE:
                    return "[Voice message]";
                case VIDEO:
                    return "[Video message]";
                case CODE:
                    return "[Code snippet]";
                default:
                    return "[Message]";
            }
        }
        
        return this.content;
    }

    /**
     * Marks the message as edited
     */
    public void markAsEdited() {
        this.isEdited = true;
        this.editedAt = LocalDateTime.now();
    }

    /**
     * Marks the message as deleted
     */
    public void markAsDeleted(User deletedBy) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedByUser = deletedBy;
    }
}