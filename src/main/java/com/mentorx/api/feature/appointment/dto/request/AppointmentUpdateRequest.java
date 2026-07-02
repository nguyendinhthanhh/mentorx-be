package com.mentorx.api.feature.appointment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AppointmentUpdateRequest {
    @NotBlank(message = "Meeting URL cannot be blank")
    private String meetingUrl;
}
