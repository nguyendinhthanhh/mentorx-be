package com.mentorx.api.feature.course.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mentorx.api.common.enums.LessonType;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.course.dto.request.QuizQuestionRequest;
import com.mentorx.api.feature.course.dto.request.QuizSubmitRequest;
import com.mentorx.api.feature.course.dto.request.QuizSubmittedAnswerRequest;
import com.mentorx.api.feature.course.dto.response.QuizAttemptResponse;
import com.mentorx.api.feature.course.dto.response.QuizQuestionResponse;
import com.mentorx.api.feature.course.entity.CourseEnrollment;
import com.mentorx.api.feature.course.entity.CourseLesson;
import com.mentorx.api.feature.course.entity.QuizAnswer;
import com.mentorx.api.feature.course.entity.QuizAttempt;
import com.mentorx.api.feature.course.entity.QuizQuestion;
import com.mentorx.api.feature.course.enums.QuizQuestionType;
import com.mentorx.api.feature.course.repository.CourseEnrollmentRepository;
import com.mentorx.api.feature.course.repository.CourseLessonRepository;
import com.mentorx.api.feature.course.repository.QuizAnswerRepository;
import com.mentorx.api.feature.course.repository.QuizAttemptRepository;
import com.mentorx.api.feature.course.repository.QuizQuestionRepository;
import com.mentorx.api.feature.course.service.LessonProgressService;
import com.mentorx.api.feature.course.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizServiceImpl implements QuizService {

    private static final BigDecimal DEFAULT_PASSING_PERCENT = BigDecimal.valueOf(50);

    private final QuizQuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final QuizAnswerRepository answerRepository;
    private final CourseLessonRepository lessonRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final LessonProgressService lessonProgressService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public QuizQuestionResponse createQuestion(UUID lessonId, QuizQuestionRequest request) {
        CourseLesson lesson = requireQuizLesson(lessonId);
        QuizQuestion question = QuizQuestion.builder()
                .lesson(lesson)
                .questionType(request.getQuestionType())
                .questionText(request.getQuestionText())
                .answerDataJson(request.getAnswerDataJson())
                .points(request.getPoints() == null ? 1 : request.getPoints())
                .explanation(request.getExplanation())
                .orderIndex(request.getOrderIndex() == null ? nextOrder(lessonId) : request.getOrderIndex())
                .build();
        return toResponse(questionRepository.save(question));
    }

    @Override
    public List<QuizQuestionResponse> getQuestions(UUID lessonId) {
        return questionRepository.findByLessonIdOrderByOrderIndexAsc(lessonId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public QuizQuestionResponse updateQuestion(UUID questionId, QuizQuestionRequest request) {
        QuizQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Quiz question not found"));
        question.setQuestionType(request.getQuestionType());
        question.setQuestionText(request.getQuestionText());
        question.setAnswerDataJson(request.getAnswerDataJson());
        question.setPoints(request.getPoints() == null ? question.getPoints() : request.getPoints());
        question.setExplanation(request.getExplanation());
        question.setOrderIndex(request.getOrderIndex() == null ? question.getOrderIndex() : request.getOrderIndex());
        return toResponse(questionRepository.save(question));
    }

    @Override
    @Transactional
    public void deleteQuestion(UUID questionId) {
        questionRepository.deleteById(questionId);
    }

    @Override
    @Transactional
    public QuizAttemptResponse submitAttempt(QuizSubmitRequest request) {
        CourseEnrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));
        CourseLesson lesson = requireQuizLesson(request.getLessonId());
        List<QuizQuestion> questions = questionRepository.findByLessonIdOrderByOrderIndexAsc(lesson.getId());
        Map<UUID, QuizQuestion> questionById = questions.stream()
                .collect(Collectors.toMap(QuizQuestion::getId, Function.identity()));

        QuizAttempt attempt = attemptRepository.save(QuizAttempt.builder()
                .enrollment(enrollment)
                .lesson(lesson)
                .startedAt(LocalDateTime.now())
                .build());

        BigDecimal score = BigDecimal.ZERO;
        BigDecimal maxScore = questions.stream()
                .map(question -> BigDecimal.valueOf(question.getPoints()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (QuizSubmittedAnswerRequest submitted : request.getAnswers()) {
            QuizQuestion question = questionById.get(submitted.getQuestionId());
            if (question == null) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Answer references a question outside this lesson");
            }
            boolean correct = isCorrect(question, submitted.getGivenAnswerJson());
            BigDecimal earned = correct ? BigDecimal.valueOf(question.getPoints()) : BigDecimal.ZERO;
            score = score.add(earned);
            answerRepository.save(QuizAnswer.builder()
                    .attempt(attempt)
                    .question(question)
                    .givenAnswerJson(submitted.getGivenAnswerJson())
                    .givenAnswer(submitted.getGivenAnswerJson())
                    .isCorrect(correct)
                    .pointsEarned(earned)
                    .build());
        }

        BigDecimal percent = maxScore.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : score.multiply(BigDecimal.valueOf(100)).divide(maxScore, 2, java.math.RoundingMode.HALF_UP);
        boolean passed = percent.compareTo(resolvePassingPercent(lesson)) >= 0;
        attempt.setScore(score);
        attempt.setMaxScore(maxScore);
        attempt.setPassed(passed);
        attempt.setCompletedAt(LocalDateTime.now());
        QuizAttempt saved = attemptRepository.save(attempt);
        if (passed) {
            lessonProgressService.markLessonAsCompleted(enrollment.getId(), lesson.getId());
        }
        return toResponse(saved);
    }

    private CourseLesson requireQuizLesson(UUID lessonId) {
        CourseLesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        if (lesson.getLessonType() != LessonType.QUIZ) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Lesson is not a quiz");
        }
        return lesson;
    }

    private int nextOrder(UUID lessonId) {
        return questionRepository.findByLessonIdOrderByOrderIndexAsc(lessonId).size() + 1;
    }

    private boolean isCorrect(QuizQuestion question, String givenAnswerJson) {
        String expectedAnswerJson = expectedAnswerJson(question);
        if (question.getQuestionType() == QuizQuestionType.TEXT_ANSWER) {
            return normalize(givenAnswerJson).equals(normalize(expectedAnswerJson));
        }
        try {
            JsonNode given = objectMapper.readTree(givenAnswerJson);
            JsonNode expected = objectMapper.readTree(expectedAnswerJson);
            return given.equals(expected);
        } catch (Exception ex) {
            return normalize(givenAnswerJson).equals(normalize(expectedAnswerJson));
        }
    }

    private String expectedAnswerJson(QuizQuestion question) {
        try {
            JsonNode answerData = objectMapper.readTree(question.getAnswerDataJson());
            JsonNode correctAnswers = question.getQuestionType() == QuizQuestionType.TEXT_ANSWER
                    ? answerData.get("correctAnswer")
                    : answerData.get("correctAnswers");
            return correctAnswers == null || correctAnswers.isNull()
                    ? ""
                    : objectMapper.writeValueAsString(correctAnswers);
        } catch (Exception ex) {
            return "";
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.replace("\"", "").trim().toLowerCase();
    }

    private BigDecimal resolvePassingPercent(CourseLesson lesson) {
        Object raw = lesson.getMetadataValue("passingPercent");
        if (raw == null) {
            return DEFAULT_PASSING_PERCENT;
        }
        try {
            BigDecimal value = new BigDecimal(String.valueOf(raw));
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                return BigDecimal.ZERO;
            }
            if (value.compareTo(BigDecimal.valueOf(100)) > 0) {
                return BigDecimal.valueOf(100);
            }
            return value;
        } catch (NumberFormatException ex) {
            return DEFAULT_PASSING_PERCENT;
        }
    }

    private QuizQuestionResponse toResponse(QuizQuestion question) {
        return QuizQuestionResponse.builder()
                .id(question.getId())
                .lessonId(question.getLesson().getId())
                .questionType(question.getQuestionType())
                .questionText(question.getQuestionText())
                .answerDataJson(question.getAnswerDataJson())
                .points(question.getPoints())
                .explanation(question.getExplanation())
                .orderIndex(question.getOrderIndex())
                .build();
    }

    private QuizAttemptResponse toResponse(QuizAttempt attempt) {
        return QuizAttemptResponse.builder()
                .id(attempt.getId())
                .enrollmentId(attempt.getEnrollment().getId())
                .lessonId(attempt.getLesson().getId())
                .score(attempt.getScore())
                .maxScore(attempt.getMaxScore())
                .passed(attempt.getPassed())
                .startedAt(attempt.getStartedAt())
                .completedAt(attempt.getCompletedAt())
                .build();
    }
}
