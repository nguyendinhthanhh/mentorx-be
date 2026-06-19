package com.mentorx.api.feature.course.service.impl;

import com.mentorx.api.common.exception.ErrorCode;

import com.mentorx.api.common.enums.CourseStatus;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.feature.course.dto.request.CourseEnrollmentCreateRequest;
import com.mentorx.api.feature.course.dto.response.CourseEnrollmentResponse;
import com.mentorx.api.feature.course.entity.Course;
import com.mentorx.api.feature.course.entity.CourseEnrollment;
import com.mentorx.api.feature.course.mapper.CourseMapper;
import com.mentorx.api.feature.course.repository.CourseEnrollmentRepository;
import com.mentorx.api.feature.course.repository.CourseRepository;
import com.mentorx.api.feature.course.repository.LessonProgressRepository;
import com.mentorx.api.feature.course.service.CertificateService;
import com.mentorx.api.feature.course.service.CourseEnrollmentService;
import com.mentorx.api.feature.system.service.PlatformSettingService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CourseEnrollmentServiceImpl implements CourseEnrollmentService {
    private static final String PLATFORM_FEE_PERCENT_KEY = "platform_fee_percent";

    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final LessonProgressRepository progressRepository;
    private final CourseMapper mapper;
    private final CertificateService certificateService;
    private final WalletService walletService;
    private final PlatformSettingService platformSettingService;

    @Override
    @Transactional
    public CourseEnrollmentResponse createEnrollment(CourseEnrollmentCreateRequest request) {
        log.info("Creating enrollment for student: {} in course: {}", request.getStudentId(), request.getCourseId());

        if (enrollmentRepository.existsByCourseIdAndStudentId(request.getCourseId(), request.getStudentId())) {
            throw new AppException(ErrorCode.ALREADY_ENROLLED);
        }

        Course course = courseRepository.findByIdAndDeletedAtIsNull(request.getCourseId())
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Only published courses can be enrolled");
        }

        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        CourseEnrollment enrollment = mapper.toEntity(request);
        enrollment.setCourse(course);
        enrollment.setStudent(student);
        enrollment.setAmountPaidMxc(effectivePrice(course));
        enrollment.setProgressPercent(BigDecimal.ZERO);
        enrollment.setIsCompleted(false);
        enrollment.setLastAccessedAt(LocalDateTime.now());

        CourseEnrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Enrollment created successfully with ID: {}", savedEnrollment.getId());

        return mapper.toResponse(savedEnrollment);
    }

    @Override
    @Transactional
    public CourseEnrollmentResponse enrollCurrentUser(UUID courseId, UUID studentId) {
        log.info("Creating enrollment for current student: {} in course: {}", studentId, courseId);

        return enrollmentRepository.findByCourseIdAndStudentId(courseId, studentId)
                .map(mapper::toResponse)
                .orElseGet(() -> {
                    Course course = courseRepository.findByIdAndDeletedAtIsNull(courseId)
                            .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
                    if (course.getStatus() != CourseStatus.PUBLISHED) {
                        throw new AppException(ErrorCode.BAD_REQUEST, "Only published courses can be enrolled");
                    }

                    User student = userRepository.findById(studentId)
                            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                    BigDecimal amountPaid = normalizeMoney(effectivePrice(course));
                    if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
                        if (walletService.getUserAvailableBalance(studentId).compareTo(amountPaid) < 0) {
                            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
                        }
                        BigDecimal platformFee = calculatePlatformFee(amountPaid);
                        walletService.processCoursePurchase(studentId, courseId, course.getInstructor().getId(), amountPaid, platformFee);
                    }

                    CourseEnrollment enrollment = CourseEnrollment.builder()
                            .course(course)
                            .student(student)
                            .amountPaidMxc(amountPaid)
                            .progressPercent(BigDecimal.ZERO)
                            .isCompleted(false)
                            .lastAccessedAt(LocalDateTime.now())
                            .build();
                    CourseEnrollment savedEnrollment = enrollmentRepository.save(enrollment);
                    course.setTotalEnrollments(course.getTotalEnrollments() == null ? 1 : course.getTotalEnrollments() + 1);
                    log.info("Enrollment created successfully with ID: {}", savedEnrollment.getId());
                    return mapper.toResponse(savedEnrollment);
                });
    }

    @Override
    public CourseEnrollmentResponse getEnrollmentById(UUID id) {
        log.debug("Fetching enrollment by ID: {}", id);
        
        CourseEnrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

        return mapper.toResponse(enrollment);
    }

    @Override
    public CourseEnrollmentResponse getEnrollmentByCourseAndStudent(UUID courseId, UUID studentId) {
        log.debug("Fetching enrollment for course: {} and student: {}", courseId, studentId);
        
        CourseEnrollment enrollment = enrollmentRepository.findByCourseIdAndStudentId(courseId, studentId)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

        return mapper.toResponse(enrollment);
    }

    @Override
    public Page<CourseEnrollmentResponse> getEnrollmentsByStudentId(UUID studentId, Pageable pageable) {
        log.debug("Fetching enrollments for student: {}", studentId);
        
        Page<CourseEnrollment> enrollments = enrollmentRepository.findByStudentId(studentId, pageable);
        return enrollments.map(mapper::toResponse);
    }

    @Override
    public Page<CourseEnrollmentResponse> getEnrollmentsByCourseId(UUID courseId, Pageable pageable) {
        log.debug("Fetching enrollments for course: {}", courseId);
        
        Page<CourseEnrollment> enrollments = enrollmentRepository.findByCourseId(courseId, pageable);
        return enrollments.map(mapper::toResponse);
    }

    @Override
    public Page<CourseEnrollmentResponse> getEnrollmentsByInstructorId(UUID instructorId, Pageable pageable) {
        log.debug("Fetching enrollments for instructor: {}", instructorId);
        
        Page<CourseEnrollment> enrollments = enrollmentRepository.findByInstructorId(instructorId, pageable);
        return enrollments.map(mapper::toResponse);
    }

    @Override
    public List<CourseEnrollmentResponse> getCompletedEnrollmentsByStudentId(UUID studentId) {
        log.debug("Fetching completed enrollments for student: {}", studentId);
        
        List<CourseEnrollment> enrollments = enrollmentRepository.findByStudentIdAndIsCompleted(studentId, true);
        return mapper.toEnrollmentResponseList(enrollments);
    }

    @Override
    @Transactional
    public void updateEnrollmentProgress(UUID enrollmentId) {
        log.info("Updating progress for enrollment: {}", enrollmentId);

        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

        Long completedLessons = progressRepository.countCompletedLessonsByEnrollmentId(enrollmentId);
        Long totalLessons = progressRepository.countTotalLessonsByEnrollmentId(enrollmentId);

        if (totalLessons > 0) {
            BigDecimal progress = BigDecimal.valueOf(completedLessons)
                    .divide(BigDecimal.valueOf(totalLessons), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            
            enrollment.setProgressPercent(progress);

            if (progress.compareTo(BigDecimal.valueOf(100)) == 0 && !enrollment.getIsCompleted()) {
                enrollment.setIsCompleted(true);
                enrollment.setCompletedAt(LocalDateTime.now());
            }

            CourseEnrollment saved = enrollmentRepository.save(enrollment);
            certificateService.issueIfEligible(saved);
            log.info("Enrollment progress updated: {}%", progress);
        }
    }

    @Override
    @Transactional
    public void markEnrollmentAsCompleted(UUID enrollmentId) {
        log.info("Marking enrollment as completed: {}", enrollmentId);

        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

        enrollment.setIsCompleted(true);
        enrollment.setCompletedAt(LocalDateTime.now());
        enrollment.setProgressPercent(BigDecimal.valueOf(100));

        CourseEnrollment saved = enrollmentRepository.save(enrollment);
        certificateService.issueIfEligible(saved);
        log.info("Enrollment marked as completed: {}", enrollmentId);
    }

    @Override
    public Long countEnrollmentsByCourseId(UUID courseId) {
        log.debug("Counting enrollments for course: {}", courseId);
        return enrollmentRepository.countByCourseId(courseId);
    }

    @Override
    public Long countEnrollmentsByStudentId(UUID studentId) {
        log.debug("Counting enrollments for student: {}", studentId);
        return enrollmentRepository.countByStudentId(studentId);
    }

    @Override
    public boolean isStudentEnrolled(UUID courseId, UUID studentId) {
        log.debug("Checking if student: {} is enrolled in course: {}", studentId, courseId);
        return enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId);
    }

    private BigDecimal effectivePrice(Course course) {
        LocalDateTime now = LocalDateTime.now();
        boolean activeDiscount = course.getDiscountPriceMxc() != null
                && course.getDiscountStartAt() != null
                && course.getDiscountEndAt() != null
                && !now.isBefore(course.getDiscountStartAt())
                && now.isBefore(course.getDiscountEndAt());
        return activeDiscount ? course.getDiscountPriceMxc() : course.getPriceMxc();
    }

    private BigDecimal calculatePlatformFee(BigDecimal amountPaid) {
        try {
            BigDecimal feePercent = new BigDecimal(platformSettingService.getValue(PLATFORM_FEE_PERCENT_KEY));
            if (feePercent.compareTo(BigDecimal.ZERO) <= 0) {
                return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }
            return amountPaid
                    .multiply(feePercent)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } catch (RuntimeException ex) {
            log.warn("Unable to resolve platform fee percent for course purchase. Defaulting fee to zero.", ex);
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }

    private BigDecimal normalizeMoney(BigDecimal amount) {
        return (amount == null ? BigDecimal.ZERO : amount).setScale(2, RoundingMode.HALF_UP);
    }
}
