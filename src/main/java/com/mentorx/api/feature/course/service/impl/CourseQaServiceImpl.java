package com.mentorx.api.feature.course.service.impl;

import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.course.dto.request.CourseQaMessageRequest;
import com.mentorx.api.feature.course.dto.response.CourseQaMessageResponse;
import com.mentorx.api.feature.course.dto.response.CourseQaSummaryResponse;
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

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
            if (!lesson.getSection().getCourse().getId().equals(courseId)) {
                throw new AppException(ErrorCode.ACCESS_DENIED);
            }
        }
        User recipient = resolveRecipient(course, senderId, request.getRecipientId());
        CourseQaMessage saved = messageRepository.save(CourseQaMessage.builder()
                .course(course)
                .lesson(lesson)
                .sender(sender)
                .recipient(recipient)
                .content(request.getContent())
                .build());
        return toResponse(saved);
    }

    @Override
    public List<CourseQaMessageResponse> recent(UUID courseId, UUID userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        if (!canAccess(course, userId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        return messageRepository.findTop100ByCourseIdOrderByCreatedAtDesc(courseId).stream()
                .sorted(Comparator.comparing(CourseQaMessage::getCreatedAt))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<CourseQaSummaryResponse> unansweredSummariesForMentor(UUID mentorId) {
        return summarize(messageRepository.findByMentorAndCourseStatus(mentorId, CourseStatus.PUBLISHED), mentorId)
                .entrySet()
                .stream()
                .map(entry -> CourseQaSummaryResponse.builder()
                        .courseId(entry.getKey())
                        .unansweredLearners(entry.getValue())
                        .build())
                .toList();
    }

    @Override
    public CourseQaSummaryResponse unansweredSummary(UUID courseId, UUID mentorId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        if (!course.getInstructor().getId().equals(mentorId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        int count = summarize(messageRepository.findByCourseIdOrderByCreatedAtAsc(courseId), mentorId)
                .getOrDefault(courseId, 0);
        return CourseQaSummaryResponse.builder()
                .courseId(courseId)
                .unansweredLearners(count)
                .build();
    }

    private boolean canAccess(Course course, UUID userId) {
        return course.getInstructor().getId().equals(userId)
                || enrollmentRepository.existsByCourseIdAndStudentId(course.getId(), userId);
    }

    private User resolveRecipient(Course course, UUID senderId, UUID recipientId) {
        if (recipientId == null) {
            return null;
        }
        if (!course.getInstructor().getId().equals(senderId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        if (!enrollmentRepository.existsByCourseIdAndStudentId(course.getId(), recipientId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        return userRepository.findById(recipientId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private Map<UUID, Integer> summarize(List<CourseQaMessage> messages, UUID mentorId) {
        Map<UUID, Map<UUID, ThreadState>> statesByCourse = new LinkedHashMap<>();
        for (CourseQaMessage message : messages) {
            UUID courseId = message.getCourse().getId();
            UUID senderId = message.getSender().getId();
            Map<UUID, ThreadState> courseStates = statesByCourse.computeIfAbsent(courseId, ignored -> new LinkedHashMap<>());
            if (senderId.equals(mentorId)) {
                if (message.getRecipient() != null) {
                    courseStates.computeIfAbsent(message.getRecipient().getId(), ignored -> new ThreadState())
                            .latestMentorReplyAt = message.getCreatedAt();
                }
                continue;
            }
            courseStates.computeIfAbsent(senderId, ignored -> new ThreadState())
                    .latestLearnerMessageAt = message.getCreatedAt();
        }

        Map<UUID, Integer> countsByCourse = new LinkedHashMap<>();
        statesByCourse.forEach((courseId, threadStates) -> {
            int count = (int) threadStates.values().stream()
                    .filter(ThreadState::isUnanswered)
                    .count();
            countsByCourse.put(courseId, count);
        });
        return countsByCourse;
    }

    private CourseQaMessageResponse toResponse(CourseQaMessage message) {
        return CourseQaMessageResponse.builder()
                .id(message.getId())
                .courseId(message.getCourse().getId())
                .lessonId(message.getLesson() == null ? null : message.getLesson().getId())
                .senderId(message.getSender().getId())
                .recipientId(message.getRecipient() == null ? null : message.getRecipient().getId())
                .senderName(message.getSender().getFullName())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private static class ThreadState {
        private LocalDateTime latestLearnerMessageAt;
        private LocalDateTime latestMentorReplyAt;

        private boolean isUnanswered() {
            return latestLearnerMessageAt != null
                    && (latestMentorReplyAt == null || latestLearnerMessageAt.isAfter(latestMentorReplyAt));
        }
    }
}
