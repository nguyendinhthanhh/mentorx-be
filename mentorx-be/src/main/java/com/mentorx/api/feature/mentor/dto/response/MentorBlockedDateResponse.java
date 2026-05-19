package com.mentorx.api.feature.mentor.dto.response;

import com.mentorx.api.feature.mentor.entity.MentorBlockedDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MentorBlockedDateResponse {

    private UUID id;
    private UUID mentorProfileId;
    private LocalDate blockedDate;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MentorBlockedDateResponse fromEntity(MentorBlockedDate entity) {
        MentorBlockedDateResponse response = new MentorBlockedDateResponse();
        response.setId(entity.getId());
        response.setMentorProfileId(entity.getMentorProfileId());
        response.setBlockedDate(entity.getBlockedDate());
        response.setReason(entity.getReason());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}
