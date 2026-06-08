package com.mentorx.api.feature.course.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.course.dto.request.CourseQaMessageRequest;
import com.mentorx.api.feature.course.dto.response.CourseQaMessageResponse;
import com.mentorx.api.feature.course.entity.Course;
import com.mentorx.api.feature.course.entity.CourseLesson;
import com.mentorx.api.feature.course.entity.CourseQaMessage;
import com.mentorx.api.feature.course.repository.CourseEnrollmentRepository;
import com.mentorx.api.feature.course.repository.CourseLessonRepository;
import com.mentorx.api.feature.course.repository.CourseQaMessageRepository;
import com.mentorx.api.feature.course.repository.CourseRepository;
import com.mentorx.api.feature.course.service.CourseQaService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseQaServiceImpl implements CourseQaService {

    private final CourseQaMessageRepository messageRepository;
    private final CourseRepository courseRepository;
    private final CourseLessonRepository lessonRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CourseQaMessageResponse send(UUID courseId, UUID senderId, CourseQaMessageRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!canAccess(course, senderId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        CourseLesson lesson = null;
        if (request.getLessonId() != null) {
            lesson = lessonRepository.findById(request.getLessonId())
                    .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        }
        CourseQaMessage saved = messageRepository.save(CourseQaMessage.builder()
                .course(course)
                .lesson(lesson)
                .sender(sender)
                .content(request.getContent())
                .build());
        return toResponse(saved);
    }

    @Override
    public List<CourseQaMessageResponse> recent(UUID courseId) {
        return messageRepository.findTop100ByCourseIdOrderByCreatedAtDesc(courseId).stream()
                .sorted(Comparator.comparing(CourseQaMessage::getCreatedAt))
                .map(this::toResponse)
                .toList();
    }

    private boolean canAccess(Course course, UUID userId) {
        return course.getInstructor().getId().equals(userId)
                || enrollmentRepository.existsByCourseIdAndStudentId(course.getId(), userId);
    }

    private CourseQaMessageResponse toResponse(CourseQaMessage message) {
        return CourseQaMessageResponse.builder()
                .id(message.getId())
                .courseId(message.getCourse().getId())
                .lessonId(message.getLesson() == null ? null : message.getLesson().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
