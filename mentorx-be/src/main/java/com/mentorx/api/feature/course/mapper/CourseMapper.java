package com.mentorx.api.feature.course.mapper;

import com.mentorx.api.feature.course.dto.request.*;
import com.mentorx.api.feature.course.dto.response.*;
import com.mentorx.api.feature.course.entity.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CourseMapper {

    // Course mappings
    @Mapping(target = "instructorId", source = "instructor.id")
    @Mapping(target = "instructorName", source = "instructor.fullName")
    @Mapping(target = "reviewedBy", source = "reviewedBy.id")
    CourseResponse toResponse(Course course);

    List<CourseResponse> toResponseList(List<Course> courses);

    @Mapping(target = "instructor", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "reviewedBy", ignore = true)
    Course toEntity(CourseCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "instructor", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "totalDurationMin", ignore = true)
    @Mapping(target = "totalLessons", ignore = true)
    @Mapping(target = "totalEnrollments", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalReviews", ignore = true)
    void updateEntity(CourseUpdateRequest request, @MappingTarget Course course);

    // CourseSection mappings
    @Mapping(target = "courseId", source = "course.id")
    CourseSectionResponse toResponse(CourseSection section);

    List<CourseSectionResponse> toSectionResponseList(List<CourseSection> sections);

    @Mapping(target = "course", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "durationMinutes", ignore = true)
    CourseSection toEntity(CourseSectionCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "durationMinutes", ignore = true)
    void updateEntity(CourseSectionUpdateRequest request, @MappingTarget CourseSection section);

    // CourseLesson mappings
    @Mapping(target = "sectionId", source = "section.id")
    CourseLessonResponse toResponse(CourseLesson lesson);

    List<CourseLessonResponse> toLessonResponseList(List<CourseLesson> lessons);

    @Mapping(target = "section", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "avgCompletionTime", ignore = true)
    CourseLesson toEntity(CourseLessonCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "avgCompletionTime", ignore = true)
    void updateEntity(CourseLessonUpdateRequest request, @MappingTarget CourseLesson lesson);

    // CourseEnrollment mappings
    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "courseTitle", source = "course.title")
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentName", source = "student.fullName")
    CourseEnrollmentResponse toResponse(CourseEnrollment enrollment);

    List<CourseEnrollmentResponse> toEnrollmentResponseList(List<CourseEnrollment> enrollments);

    @Mapping(target = "course", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "progressPercent", ignore = true)
    @Mapping(target = "isCompleted", ignore = true)
    @Mapping(target = "certificateUrl", ignore = true)
    @Mapping(target = "enrolledAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    CourseEnrollment toEntity(CourseEnrollmentCreateRequest request);

    // LessonProgress mappings
    @Mapping(target = "enrollmentId", source = "id.enrollmentId")
    @Mapping(target = "lessonId", source = "id.lessonId")
    @Mapping(target = "lessonTitle", source = "lesson.title")
    LessonProgressResponse toResponse(LessonProgress progress);

    List<LessonProgressResponse> toProgressResponseList(List<LessonProgress> progressList);

    // LessonComment mappings
    @Mapping(target = "lessonId", source = "lesson.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.fullName")
    @Mapping(target = "userAvatar", source = "user.avatarUrl")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "replies", ignore = true)
    LessonCommentResponse toResponse(LessonComment comment);

    List<LessonCommentResponse> toCommentResponseList(List<LessonComment> comments);

    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    LessonComment toEntity(LessonCommentCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntity(LessonCommentUpdateRequest request, @MappingTarget LessonComment comment);

    // CartItem mappings
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "courseTitle", source = "course.title")
    @Mapping(target = "courseThumbnail", source = "course.thumbnailUrl")
    @Mapping(target = "coursePriceMxc", source = "course.priceMxc")
    @Mapping(target = "instructorName", source = "course.instructor.fullName")
    CartItemResponse toResponse(CartItem cartItem);

    List<CartItemResponse> toCartItemResponseList(List<CartItem> cartItems);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "addedAt", ignore = true)
    CartItem toEntity(CartItemCreateRequest request);
}
