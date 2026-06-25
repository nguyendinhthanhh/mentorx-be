package com.mentorx.api.feature.appointment.repository;

import com.mentorx.api.feature.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByUserIdOrderByStartTimeDesc(UUID userId);

    List<Appointment> findByMentorIdOrderByStartTimeDesc(UUID mentorId);

}
