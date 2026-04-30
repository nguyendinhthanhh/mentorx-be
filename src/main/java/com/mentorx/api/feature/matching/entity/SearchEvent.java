package com.mentorx.api.feature.matching.entity;

import com.mentorx.api.common.enums.JobType;
import com.mentorx.api.feature.system.entity.Category;
import com.mentorx.api.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "search_events")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "query_text", columnDefinition = "TEXT")
    private String queryText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filter_category_id")
    private Category filterCategory;

    @Column(name = "filter_skill_ids")
    private List<Integer> filterSkillIds;

    @Enumerated(EnumType.STRING)
    @Column(name = "filter_job_type")
    private JobType filterJobType;

    @Column(name = "filter_budget_min", precision = 12, scale = 2)
    private BigDecimal filterBudgetMin;

    @Column(name = "filter_budget_max", precision = 12, scale = 2)
    private BigDecimal filterBudgetMax;

    @Column(name = "search_context", length = 30)
    private String searchContext;

    @Column(name = "result_count")
    private Integer resultCount;

    @Column(name = "clicked_result_id")
    private UUID clickedResultId;

    @Column(name = "clicked_result_type", length = 30)
    private String clickedResultType;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
