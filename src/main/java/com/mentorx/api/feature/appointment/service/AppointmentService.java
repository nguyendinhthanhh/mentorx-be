package com.mentorx.api.feature.appointment.service;

import com.mentorx.api.common.exception.ResourceNotFoundException;
import com.mentorx.api.feature.appointment.dto.request.AppointmentCreateRequest;
import com.mentorx.api.feature.appointment.dto.response.AppointmentResponse;
import com.mentorx.api.feature.appointment.entity.Appointment;
import com.mentorx.api.feature.appointment.enums.AppointmentStatus;
import com.mentorx.api.feature.appointment.mapper.AppointmentMapper;
import com.mentorx.api.feature.appointment.repository.AppointmentRepository;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final AppointmentMapper appointmentMapper;

    @Transactional
    public AppointmentResponse bookAppointment(UUID userId, AppointmentCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User mentor = userRepository.findById(request.getMentorId())
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));

        Appointment appointment = Appointment.builder()
                .user(user)
                .mentor(mentor)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(AppointmentStatus.SCHEDULED)
                .notes(request.getNotes())
                .build();

        appointment = appointmentRepository.save(appointment);
        return appointmentMapper.toResponse(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getUserAppointments(UUID userId) {
        return appointmentRepository.findByUserIdOrderByStartTimeDesc(userId)
                .stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMentorAppointments(UUID mentorId) {
        return appointmentRepository.findByMentorIdOrderByStartTimeDesc(mentorId)
                .stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse cancelAppointment(UUID id, UUID currentUserId) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
                
        if (!appointment.getUser().getId().equals(currentUserId) && !appointment.getMentor().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("Not authorized to cancel this appointment");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment = appointmentRepository.save(appointment);
        return appointmentMapper.toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse updateMeetingUrl(UUID id, UUID mentorId, String meetingUrl) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (!appointment.getMentor().getId().equals(mentorId)) {
            throw new IllegalArgumentException("Not authorized to update this appointment");
        }

        appointment.setMeetingUrl(meetingUrl);
        appointment = appointmentRepository.save(appointment);
        return appointmentMapper.toResponse(appointment);
    }
    
    @Transactional
    public AppointmentResponse completeAppointment(UUID id, UUID mentorId) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (!appointment.getMentor().getId().equals(mentorId)) {
            throw new IllegalArgumentException("Not authorized to complete this appointment");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment = appointmentRepository.save(appointment);
        return appointmentMapper.toResponse(appointment);
    }
}
