package com.mentorx.api.feature.course.entity;

import com.mentorx.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course_sections")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CourseSection extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @NotNull
    @Size(max = 200)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @NotNull
    @Min(value = 1)
    @Column(name = "section_order", nullable = false)
    private Integer sectionOrder;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL)
    private List<CourseLesson> lessons = new ArrayList<>();

    @Column(name = "is_published", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isPublished = true;
}