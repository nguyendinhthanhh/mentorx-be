package com.mentorx.api.feature.mentor.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MentorBlockedDateRequest {

    @NotNull(message = "Blocked date is required")
    private LocalDate blockedDate;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}
