package com.mentorx.api.feature.mentor.dto.response;

import com.mentorx.api.common.enums.CourseLevel;
import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.feature.mentor.entity.MentorOffering;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MentorOfferingResponse {

    private UUID id;
    private UUID mentorProfileId;
    private String title;
    private String description;
    private BigDecimal priceMxc;
    private Integer durationHours;
    private CourseLevel level;
    private Integer lessonsCount;
    private String thumbnailUrl;
    private CourseStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MentorOfferingResponse fromEntity(MentorOffering entity) {
        MentorOfferingResponse response = new MentorOfferingResponse();
        response.setId(entity.getId());
        response.setMentorProfileId(entity.getMentorProfileId());
        response.setTitle(entity.getTitle());
        response.setDescription(entity.getDescription());
        response.setPriceMxc(entity.getPriceMxc());
        response.setDurationHours(entity.getDurationHours());
        response.setLevel(entity.getLevel());
        response.setLessonsCount(entity.getLessonsCount());
        response.setThumbnailUrl(entity.getThumbnailUrl());
        response.setStatus(entity.getStatus());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}

