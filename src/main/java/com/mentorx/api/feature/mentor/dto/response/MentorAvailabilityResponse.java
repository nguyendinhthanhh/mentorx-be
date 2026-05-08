package com.mentorx.api.feature.mentor.dto.response;

import com.mentorx.api.feature.mentor.entity.MentorAvailability;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MentorAvailabilityResponse {

    private UUID id;
    private UUID mentorProfileId;
    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MentorAvailabilityResponse fromEntity(MentorAvailability entity) {
        MentorAvailabilityResponse response = new MentorAvailabilityResponse();
        response.setId(entity.getId());
        response.setMentorProfileId(entity.getMentorProfileId());
        response.setDayOfWeek(entity.getDayOfWeek());
        response.setStartTime(entity.getStartTime());
        response.setEndTime(entity.getEndTime());
        response.setIsActive(entity.getIsActive());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}
