package com.mentorx.api.feature.appointment.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.common.util.SecurityUtils;
import com.mentorx.api.feature.appointment.dto.request.AppointmentCreateRequest;
import com.mentorx.api.feature.appointment.dto.request.AppointmentUpdateRequest;
import com.mentorx.api.feature.appointment.dto.response.AppointmentResponse;
import com.mentorx.api.feature.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AppointmentResponse>> bookAppointment(
            @Valid @RequestBody AppointmentCreateRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        AppointmentResponse response = appointmentService.bookAppointment(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("Appointment booked successfully", response));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getUserAppointments(
            @PathVariable UUID userId) {
        List<AppointmentResponse> responses = appointmentService.getUserAppointments(userId);
        return ResponseEntity.ok(ApiResponse.success("User appointments retrieved", responses));
    }

    @GetMapping("/mentor/{mentorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getMentorAppointments(
            @PathVariable UUID mentorId) {
        List<AppointmentResponse> responses = appointmentService.getMentorAppointments(mentorId);
        return ResponseEntity.ok(ApiResponse.success("Mentor appointments retrieved", responses));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(
            @PathVariable UUID id) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        AppointmentResponse response = appointmentService.cancelAppointment(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled", response));
    }

    @PutMapping("/{id}/meeting-url")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateMeetingUrl(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentUpdateRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        AppointmentResponse response = appointmentService.updateMeetingUrl(id, currentUserId, request.getMeetingUrl());
        return ResponseEntity.ok(ApiResponse.success("Meeting URL updated", response));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AppointmentResponse>> completeAppointment(
            @PathVariable UUID id) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        AppointmentResponse response = appointmentService.completeAppointment(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Appointment marked as completed", response));
    }
}
