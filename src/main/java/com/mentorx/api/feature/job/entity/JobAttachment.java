package com.mentorx.api.feature.job.entity;

import com.mentorx.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity representing attachments for job postings
 * 
 * @author MentorX Development Team
 * @since 1.0.0
 */
@Entity
@Table(name = "job_attachments", indexes = {
    @Index(name = "idx_job_attachment_job_id", columnList = "job_id"),
    @Index(name = "idx_job_attachment_type", columnList = "file_type")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class JobAttachment extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @NotNull
    @Size(max = 255)
    @Column(name = "filename", nullable = false, length = 255)
    private String filename;

    @NotNull
    @Size(max = 500)
    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Size(max = 100)
    @Column(name = "file_type", length = 100)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Size(max = 200)
    @Column(name = "description", length = 200)
    private String description;
}