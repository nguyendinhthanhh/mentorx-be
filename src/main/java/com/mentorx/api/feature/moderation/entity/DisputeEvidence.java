package com.mentorx.api.feature.moderation.entity;

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
 * Entity representing evidence submitted in a dispute
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "dispute_evidence", indexes = {
    @Index(name = "idx_dispute_evidence_dispute_id", columnList = "dispute_id"),
    @Index(name = "idx_dispute_evidence_submitted_by", columnList = "submitted_by_user_id"),
    @Index(name = "idx_dispute_evidence_type", columnList = "evidence_type"),
    @Index(name = "idx_dispute_evidence_created", columnList = "created_at DESC")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DisputeEvidence extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispute_id", nullable = false)
    private Dispute dispute;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_user_id", nullable = false)
    private User submittedByUser;

    /**
     * Type of evidence
     */
    @NotNull
    @Size(max = 30)
    @Column(name = "evidence_type", nullable = false, length = 30)
    private String evidenceType; // SCREENSHOT, DOCUMENT, VIDEO, AUDIO, CHAT_LOG, EMAIL, CONTRACT, INVOICE, OTHER

    /**
     * Title/description of the evidence
     */
    @NotNull
    @Size(max = 200)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * Detailed description
     */
    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    /**
     * URL to the evidence file
     */
    @Size(max = 500)
    @Column(name = "file_url", length = 500)
    private String fileUrl;

    /**
     * Original filename
     */
    @Size(max = 255)
    @Column(name = "filename", length = 255)
    private String filename;

    /**
     * MIME type of the file
     */
    @Size(max = 100)
    @Column(name = "mime_type", length = 100)
    private String mimeType;

    /**
     * File size in bytes
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * When the evidence was submitted
     */
    @NotNull
    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    /**
     * Whether this evidence has been reviewed
     */
    @Column(name = "is_reviewed", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isReviewed = false;

    /**
     * When the evidence was reviewed
     */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    /**
     * Who reviewed the evidence
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedByUser;

    /**
     * Notes from the reviewer
     */
    @Size(max = 1000)
    @Column(name = "review_notes", length = 1000)
    private String reviewNotes;

    /**
     * Whether this evidence is considered valid
     */
    @Column(name = "is_valid")
    private Boolean isValid;

    /**
     * Relevance score (0-10)
     */
    @Column(name = "relevance_score")
    private Integer relevanceScore;

    /**
     * Whether this evidence is confidential
     */
    @Column(name = "is_confidential", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isConfidential = false;

    /**
     * Order/sequence number for display
     */
    @Column(name = "display_order")
    private Integer displayOrder;

    /**
     * Whether this evidence has been flagged for review
     */
    @Column(name = "is_flagged", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isFlagged = false;

    /**
     * Reason for flagging
     */
    @Size(max = 200)
    @Column(name = "flag_reason", length = 200)
    private String flagReason;

    @PrePersist
    protected void onCreate() {
        if (this.submittedAt == null) {
            this.submittedAt = LocalDateTime.now();
        }
    }

    /**
     * Marks the evidence as reviewed
     */
    public void markAsReviewed(User reviewer, boolean isValid, String notes) {
        this.isReviewed = true;
        this.reviewedAt = LocalDateTime.now();
        this.reviewedByUser = reviewer;
        this.isValid = isValid;
        this.reviewNotes = notes;
    }

    /**
     * Flags the evidence for review
     */
    public void flag(String reason) {
        this.isFlagged = true;
        this.flagReason = reason;
    }

    /**
     * Checks if the evidence is a file attachment
     */
    public boolean hasFile() {
        return this.fileUrl != null && !this.fileUrl.trim().isEmpty();
    }
}