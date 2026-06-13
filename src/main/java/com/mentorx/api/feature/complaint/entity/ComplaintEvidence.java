package com.mentorx.api.feature.complaint.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.complaint.enums.EvidenceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dispute_evidence", indexes = {
    @Index(name = "idx_dispute_evidence_dispute_id", columnList = "dispute_id"),
    @Index(name = "idx_dispute_evidence_submitted_by", columnList = "submitted_by_user_id"),
    @Index(name = "idx_dispute_evidence_created", columnList = "created_at DESC")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintEvidence extends BaseEntity {

    @Column(name = "dispute_id", nullable = false)
    private UUID disputeId;

    @Column(name = "submitted_by_user_id", nullable = false)
    private UUID submittedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_type", nullable = false, length = 30)
    private EvidenceType evidenceType;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "filename", length = 255)
    private String filename;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "is_reviewed", nullable = false)
    @Builder.Default
    private Boolean isReviewed = false;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "reviewed_by_user_id")
    private UUID reviewedByUserId;

    @Column(name = "review_notes", length = 1000)
    private String reviewNotes;

    @Column(name = "is_flagged", nullable = false)
    @Builder.Default
    private Boolean isFlagged = false;

    @Column(name = "flag_reason", length = 500)
    private String flagReason;

    public void markAsReviewed(UUID reviewerId, boolean approved, String notes) {
        this.isReviewed = true;
        this.reviewedAt = LocalDateTime.now();
        this.reviewedByUserId = reviewerId;
        this.reviewNotes = notes;
    }

    public void flag(String reason) {
        this.isFlagged = true;
        this.flagReason = reason;
    }
}
