package com.mentorx.api.feature.appointment.mapper;

import com.mentorx.api.feature.appointment.dto.response.AppointmentResponse;
import com.mentorx.api.feature.appointment.entity.Appointment;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public AppointmentResponse toResponse(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        
        return AppointmentResponse.builder()
                .id(appointment.getId().toString())
                .mentorId(appointment.getMentor().getId().toString())
                .mentorName(appointment.getMentor().getFullName())
                .userId(appointment.getUser().getId().toString())
                .userName(appointment.getUser().getFullName())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .status(appointment.getStatus())
                .meetingUrl(appointment.getMeetingUrl())
                .notes(appointment.getNotes())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
}
