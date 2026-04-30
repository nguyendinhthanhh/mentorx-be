package com.mentorx.api.feature.matching.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.user.entity.MentorProfile;
import com.mentorx.api.feature.system.entity.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity representing featured mentor slots for promoted visibility
 * Manages paid promotions and premium placement in recommendations
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "featured_mentor_slots", indexes = {
    @Index(name = "idx_featured_mentor_id", columnList = "mentor_profile_id"),
    @Index(name = "idx_featured_category_id", columnList = "category_id"),
    @Index(name = "idx_featured_active", columnList = "is_active, start_date, end_date"),
    @Index(name = "idx_featured_priority", columnList = "priority_level DESC, start_date ASC"),
    @Index(name = "idx_featured_dates", columnList = "start_date, end_date"),
    @Index(name = "idx_featured_category_active", columnList = "category_id, is_active, priority_level DESC")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class FeaturedMentorSlot extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_profile_id", nullable = false)
    private MentorProfile mentorProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category; // null means featured across all categories

    /**
     * When the featured placement starts
     */
    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /**
     * When the featured placement ends
     */
    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /**
     * Priority level (higher = more prominent placement)
     */
    @Min(value = 1)
    @Column(name = "priority_level", nullable = false, columnDefinition = "INTEGER DEFAULT 1")
    private Integer priorityLevel = 1;

    /**
     * Whether this featured slot is currently active
     */
    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive = true;

    /**
     * Cost paid for this featured placement (in MXC)
     */
    @DecimalMin(value = "0.0")
    @Column(name = "cost_mxc", precision = 10, scale = 2)
    private BigDecimal costMxc;

    /**
     * Type of featured placement (banner, spotlight, premium_listing, etc.)
     */
    @NotNull
    @Size(max = 30)
    @Column(name = "placement_type", nullable = false, length = 30)
    private String placementType;

    /**
     * Custom title for the featured placement
     */
    @Size(max = 200)
    @Column(name = "custom_title", length = 200)
    private String customTitle;

    /**
     * Custom description for the featured placement
     */
    @Size(max = 500)
    @Column(name = "custom_description", length = 500)
    private String customDescription;

    /**
     * Custom image URL for the featured placement
     */
    @Size(max = 500)
    @Column(name = "custom_image_url", length = 500)
    private String customImageUrl;

    /**
     * Target audience criteria as JSON
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "target_criteria", columnDefinition = "jsonb")
    private Map<String, Object> targetCriteria;

    /**
     * Number of impressions (times shown to users)
     */
    @Min(value = 0)
    @Column(name = "impression_count", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long impressionCount = 0L;

    /**
     * Number of clicks received
     */
    @Min(value = 0)
    @Column(name = "click_count", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long clickCount = 0L;

    /**
     * Number of conversions (profile views, contact attempts, etc.)
     */
    @Min(value = 0)
    @Column(name = "conversion_count", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long conversionCount = 0L;

    /**
     * Click-through rate (calculated field)
     */
    @Column(name = "click_through_rate", precision = 5, scale = 4)
    private BigDecimal clickThroughRate;

    /**
     * Conversion rate (calculated field)
     */
    @Column(name = "conversion_rate", precision = 5, scale = 4)
    private BigDecimal conversionRate;

    /**
     * Budget limit for this campaign (in MXC)
     */
    @DecimalMin(value = "0.0")
    @Column(name = "budget_limit_mxc", precision = 10, scale = 2)
    private BigDecimal budgetLimitMxc;

    /**
     * Amount spent so far (in MXC)
     */
    @DecimalMin(value = "0.0")
    @Column(name = "amount_spent_mxc", precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private BigDecimal amountSpentMxc = BigDecimal.ZERO;

    /**
     * Billing model (CPM, CPC, CPA, FLAT_RATE)
     */
    @Size(max = 20)
    @Column(name = "billing_model", length = 20)
    private String billingModel;

    /**
     * Rate per billing unit (per impression, click, etc.)
     */
    @DecimalMin(value = "0.0")
    @Column(name = "rate_per_unit", precision = 8, scale = 4)
    private BigDecimal ratePerUnit;

    /**
     * ID of admin who approved this featured placement
     */
    @Column(name = "approved_by_admin_id")
    private Long approvedByAdminId;

    /**
     * When this placement was approved
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * Status of the featured placement
     */
    @Size(max = 20)
    @Column(name = "status", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'PENDING'")
    private String status = "PENDING"; // PENDING, APPROVED, ACTIVE, PAUSED, COMPLETED, CANCELLED

    /**
     * Notes from admin or system
     */
    @Size(max = 1000)
    @Column(name = "notes", length = 1000)
    private String notes;

    /**
     * When statistics were last updated
     */
    @Column(name = "stats_updated_at")
    private LocalDateTime statsUpdatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        // Calculate rates if we have the data
        if (impressionCount != null && impressionCount > 0) {
            if (clickCount != null) {
                this.clickThroughRate = BigDecimal.valueOf(clickCount.doubleValue() / impressionCount.doubleValue());
            }
            if (conversionCount != null) {
                this.conversionRate = BigDecimal.valueOf(conversionCount.doubleValue() / impressionCount.doubleValue());
            }
        }
        
        this.statsUpdatedAt = LocalDateTime.now();
    }

    /**
     * Checks if this featured slot is currently active and within date range
     */
    public boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && 
               now.isAfter(startDate) && 
               now.isBefore(endDate) &&
               "ACTIVE".equals(status);
    }

    /**
     * Checks if budget limit has been reached
     */
    public boolean isBudgetExceeded() {
        return budgetLimitMxc != null && 
               amountSpentMxc != null && 
               amountSpentMxc.compareTo(budgetLimitMxc) >= 0;
    }
}