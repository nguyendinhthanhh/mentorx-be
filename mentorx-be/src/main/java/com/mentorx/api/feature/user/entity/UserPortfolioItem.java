package com.mentorx.api.feature.user.entity;

import com.mentorx.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Entity representing portfolio items for users (especially mentors)
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "user_portfolio_items", indexes = {
    @Index(name = "idx_portfolio_user_id", columnList = "user_id"),
    @Index(name = "idx_portfolio_type", columnList = "item_type"),
    @Index(name = "idx_portfolio_featured", columnList = "is_featured, display_order"),
    @Index(name = "idx_portfolio_published", columnList = "is_published"),
    @Index(name = "idx_portfolio_created", columnList = "created_at DESC")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserPortfolioItem extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Type of portfolio item
     */
    @NotNull
    @Size(max = 30)
    @Column(name = "item_type", nullable = false, length = 30)
    private String itemType; // PROJECT, CERTIFICATION, PUBLICATION, AWARD, PRESENTATION, COURSE, OPEN_SOURCE, DESIGN, VIDEO, OTHER

    /**
     * Title of the portfolio item
     */
    @NotNull
    @Size(max = 200)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * Detailed description
     */
    @Size(max = 2000)
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * URL to the portfolio item (project link, certificate, etc.)
     */
    @Size(max = 500)
    @Column(name = "url", length = 500)
    private String url;

    /**
     * Thumbnail/cover image URL
     */
    @Size(max = 500)
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    /**
     * Additional images/media URLs
     */
    @ElementCollection
    @CollectionTable(name = "portfolio_item_media", 
                    joinColumns = @JoinColumn(name = "portfolio_item_id"))
    @Column(name = "media_url", length = 500)
    private List<String> mediaUrls = new ArrayList<>();

    /**
     * Technologies/skills used (comma-separated or JSON array)
     */
    @Size(max = 500)
    @Column(name = "technologies", length = 500)
    private String technologies;

    /**
     * Role in the project
     */
    @Size(max = 100)
    @Column(name = "role", length = 100)
    private String role;

    /**
     * Organization/company name
     */
    @Size(max = 200)
    @Column(name = "organization", length = 200)
    private String organization;

    /**
     * Start date of the project/work
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * End date of the project/work
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Whether this is an ongoing project
     */
    @Column(name = "is_ongoing", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isOngoing = false;

    /**
     * Whether this item is published/visible
     */
    @Column(name = "is_published", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isPublished = true;

    /**
     * Whether this item is featured
     */
    @Column(name = "is_featured", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isFeatured = false;

    /**
     * Display order for sorting
     */
    @Column(name = "display_order", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer displayOrder = 0;

    /**
     * Number of views
     */
    @Column(name = "view_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer viewCount = 0;

    /**
     * Number of likes/endorsements
     */
    @Column(name = "like_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer likeCount = 0;

    /**
     * Tags for categorization
     */
    @ElementCollection
    @CollectionTable(name = "portfolio_item_tags", 
                    joinColumns = @JoinColumn(name = "portfolio_item_id"))
    @Column(name = "tag", length = 50)
    private List<String> tags = new ArrayList<>();

    /**
     * Additional metadata as JSON
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * Collaborators (if any)
     */
    @Size(max = 500)
    @Column(name = "collaborators", length = 500)
    private String collaborators;

    /**
     * Client name (for client projects)
     */
    @Size(max = 200)
    @Column(name = "client_name", length = 200)
    private String clientName;

    /**
     * Whether client name should be displayed
     */
    @Column(name = "show_client_name", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean showClientName = true;

    /**
     * Budget/value of the project (if applicable)
     */
    @Size(max = 100)
    @Column(name = "project_value", length = 100)
    private String projectValue;

    /**
     * Impact/results achieved
     */
    @Size(max = 1000)
    @Column(name = "impact", length = 1000)
    private String impact;

    /**
     * Testimonial/feedback received
     */
    @Size(max = 1000)
    @Column(name = "testimonial", length = 1000)
    private String testimonial;

    /**
     * Name of person who gave testimonial
     */
    @Size(max = 100)
    @Column(name = "testimonial_author", length = 100)
    private String testimonialAuthor;

    /**
     * Whether this item has been verified
     */
    @Column(name = "is_verified", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isVerified = false;

    /**
     * When the item was verified
     */
    @Column(name = "verified_at")
    private java.time.LocalDateTime verifiedAt;

    /**
     * Language of the content
     */
    @Size(max = 10)
    @Column(name = "language", length = 10)
    private String language;

    /**
     * Calculates the duration in months
     */
    public Integer getDurationInMonths() {
        if (startDate == null) {
            return null;
        }
        
        LocalDate end = isOngoing ? LocalDate.now() : endDate;
        if (end == null) {
            return null;
        }
        
        return (int) java.time.temporal.ChronoUnit.MONTHS.between(startDate, end);
    }

    /**
     * Increments view count
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * Increments like count
     */
    public void incrementLikeCount() {
        this.likeCount++;
    }

    /**
     * Decrements like count
     */
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    /**
     * Marks as verified
     */
    public void markAsVerified() {
        this.isVerified = true;
        this.verifiedAt = java.time.LocalDateTime.now();
    }

    /**
     * Checks if the item is currently active
     */
    public boolean isActive() {
        return this.isPublished && (this.isOngoing || this.endDate == null || !this.endDate.isBefore(LocalDate.now()));
    }
}