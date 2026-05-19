package com.mentorx.api.feature.job.entity;

import com.mentorx.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing deliverables for milestones
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "milestone_deliverables", indexes = {
    @Index(name = "idx_deliverable_milestone_id", columnList = "milestone_id"),
    @Index(name = "idx_deliverable_type", columnList = "deliverable_type"),
    @Index(name = "idx_deliverable_uploaded", columnList = "uploaded_at DESC")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneDeliverable extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "milestone_id", nullable = false)
    private Milestone milestone;

    /**
     * Type of deliverable
     */
    @NotNull
    @Size(max = 30)
    @Column(name = "deliverable_type", nullable = false, length = 30)
    private String deliverableType; // FILE, LINK, CODE_REPOSITORY, DOCUMENT, VIDEO, IMAGE, OTHER

    /**
     * Title/name of the deliverable
     */
    @NotNull
    @Size(max = 200)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * Description
     */
    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    /**
     * File URL or link
     */
    @NotNull
    @Size(max = 500)
    @Column(name = "url", nullable = false, length = 500)
    private String url;

    /**
     * Original filename (if file upload)
     */
    @Size(max = 255)
    @Column(name = "filename", length = 255)
    private String filename;

    /**
     * File size in bytes
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * MIME type
     */
    @Size(max = 100)
    @Column(name = "mime_type", length = 100)
    private String mimeType;

    /**
     * When uploaded
     */
    @NotNull
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    /**
     * Display order
     */
    @Column(name = "display_order")
    private Integer displayOrder;

    /**
     * Whether this deliverable has been reviewed
     */
    @Column(name = "is_reviewed", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isReviewed = false;

    /**
     * When reviewed
     */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    /**
     * Whether approved
     */
    @Column(name = "is_approved")
    private Boolean isApproved;

    /**
     * Review comments
     */
    @Size(max = 500)
    @Column(name = "review_comments", length = 500)
    private String reviewComments;

    /**
     * Number of downloads by client
     */
    @Column(name = "download_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer downloadCount = 0;

    /**
     * Last downloaded at
     */
    @Column(name = "last_downloaded_at")
    private LocalDateTime lastDownloadedAt;

    /**
     * Whether this is a revision
     */
    @Column(name = "is_revision", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isRevision = false;

    /**
     * Original deliverable ID if this is a revision
     */
    @Column(name = "original_deliverable_id")
    private Long originalDeliverableId;

    /**
     * Revision number
     */
    @Column(name = "revision_number", columnDefinition = "INTEGER DEFAULT 0")
    private Integer revisionNumber = 0;

    @PrePersist
    protected void onCreate() {
        if (this.uploadedAt == null) {
            this.uploadedAt = LocalDateTime.now();
        }
    }

    /**
     * Records a download
     */
    public void recordDownload() {
        this.downloadCount++;
        this.lastDownloadedAt = LocalDateTime.now();
    }

    /**
     * Marks as reviewed
     */
    public void markAsReviewed(boolean approved, String comments) {
        this.isReviewed = true;
        this.reviewedAt = LocalDateTime.now();
        this.isApproved = approved;
        this.reviewComments = comments;
    }
}