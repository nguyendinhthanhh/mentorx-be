package com.mentorx.api.feature.course.service.impl;

import com.mentorx.api.common.exception.ErrorCode;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.feature.course.dto.request.CourseSectionCreateRequest;
import com.mentorx.api.feature.course.dto.request.CourseSectionUpdateRequest;
import com.mentorx.api.feature.course.dto.response.CourseSectionResponse;
import com.mentorx.api.feature.course.entity.Course;
import com.mentorx.api.feature.course.entity.CourseSection;
import com.mentorx.api.feature.course.mapper.CourseMapper;
import com.mentorx.api.feature.course.repository.CourseRepository;
import com.mentorx.api.feature.course.repository.CourseSectionRepository;
import com.mentorx.api.feature.course.service.CourseSectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CourseSectionServiceImpl implements CourseSectionService {

    private final CourseSectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final CourseMapper mapper;

    @Override
    @Transactional
    public CourseSectionResponse createSection(CourseSectionCreateRequest request) {
        log.info("Creating new course section for course: {}", request.getCourseId());

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        if (sectionRepository.existsByCourseIdAndSectionOrder(request.getCourseId(), request.getSectionOrder())) {
            throw new AppException(ErrorCode.SECTION_ORDER_EXISTS);
        }

        CourseSection section = mapper.toEntity(request);
        section.setCourse(course);
        
        if (section.getIsPublished() == null) {
            section.setIsPublished(true);
        }

        CourseSection savedSection = sectionRepository.save(section);
        log.info("Course section created successfully with ID: {}", savedSection.getId());

        return mapper.toResponse(savedSection);
    }

    @Override
    public CourseSectionResponse getSectionById(UUID id) {
        log.debug("Fetching course section by ID: {}", id);
        
        CourseSection section = sectionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_SECTION_NOT_FOUND));

        return mapper.toResponse(section);
    }

    @Override
    public List<CourseSectionResponse> getSectionsByCourseId(UUID courseId) {
        log.debug("Fetching all sections for course: {}", courseId);
        
        List<CourseSection> sections = sectionRepository.findByCourseIdOrderBySectionOrderAsc(courseId);
        return mapper.toSectionResponseList(sections);
    }

    @Override
    public Page<CourseSectionResponse> getSectionsByCourseId(UUID courseId, Pageable pageable) {
        log.debug("Fetching sections for course: {} with pagination", courseId);
        
        Page<CourseSection> sections = sectionRepository.findByCourseId(courseId, pageable);
        return sections.map(mapper::toResponse);
    }

    @Override
    @Transactional
    public CourseSectionResponse updateSection(UUID id, CourseSectionUpdateRequest request) {
        log.info("Updating course section: {}", id);

        CourseSection section = sectionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_SECTION_NOT_FOUND));

        if (request.getSectionOrder() != null && 
            !request.getSectionOrder().equals(section.getSectionOrder()) &&
            sectionRepository.existsByCourseIdAndSectionOrder(section.getCourse().getId(), request.getSectionOrder())) {
            throw new AppException(ErrorCode.SECTION_ORDER_EXISTS);
        }

        mapper.updateEntity(request, section);
        CourseSection updatedSection = sectionRepository.save(section);
        
        log.info("Course section updated successfully: {}", id);
        return mapper.toResponse(updatedSection);
    }

    @Override
    @Transactional
    public void deleteSection(UUID id) {
        log.info("Deleting course section: {}", id);

        if (!sectionRepository.existsById(id)) {
            throw new AppException(ErrorCode.COURSE_SECTION_NOT_FOUND);
        }

        sectionRepository.deleteById(id);
        log.info("Course section deleted successfully: {}", id);
    }

    @Override
    public Long countSectionsByCourseId(UUID courseId) {
        log.debug("Counting sections for course: {}", courseId);
        return sectionRepository.countByCourseId(courseId);
    }

    @Override
    public List<CourseSectionResponse> getPublishedSectionsByCourseId(UUID courseId) {
        log.debug("Fetching published sections for course: {}", courseId);
        
        List<CourseSection> sections = sectionRepository.findByCourseIdAndIsPublished(courseId, true);
        return mapper.toSectionResponseList(sections);
    }
}
