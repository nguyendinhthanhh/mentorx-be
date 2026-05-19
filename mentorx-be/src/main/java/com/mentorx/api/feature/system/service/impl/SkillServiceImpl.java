package com.mentorx.api.feature.system.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.system.dto.request.SkillRequest;
import com.mentorx.api.feature.system.dto.response.SkillResponse;
import com.mentorx.api.feature.system.entity.Skill;
import com.mentorx.api.feature.system.mapper.SystemMapper;
import com.mentorx.api.feature.system.repository.SkillRepository;
import com.mentorx.api.feature.system.service.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final SystemMapper systemMapper;

    @Override
    @Transactional
    public SkillResponse create(SkillRequest request) {
        log.info("Creating skill with slug: {}", request.slug());

        if (skillRepository.existsBySlug(request.slug())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Skill with this slug already exists");
        }

        Skill entity = systemMapper.toSkill(request);
        entity.setCreatedAt(LocalDateTime.now());
        
        if (entity.getIsActive() == null) {
            entity.setIsActive(true);
        }

        Skill saved = skillRepository.save(entity);
        log.info("Created skill with ID: {}", saved.getId());

        return systemMapper.toSkillResponse(saved);
    }

    @Override
    public SkillResponse getById(Integer id) {
        log.debug("Fetching skill with ID: {}", id);
        Skill entity = skillRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return systemMapper.toSkillResponse(entity);
    }

    @Override
    public SkillResponse getBySlug(String slug) {
        log.debug("Fetching skill with slug: {}", slug);
        Skill entity = skillRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return systemMapper.toSkillResponse(entity);
    }

    @Override
    @Transactional
    public SkillResponse update(Integer id, SkillRequest request) {
        log.info("Updating skill with ID: {}", id);

        Skill entity = skillRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // Check slug uniqueness if changed
        if (request.slug() != null && !request.slug().equals(entity.getSlug())) {
            if (skillRepository.existsBySlug(request.slug())) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Skill with this slug already exists");
            }
        }

        systemMapper.updateSkill(entity, request);

        Skill updated = skillRepository.save(entity);
        log.info("Updated skill with ID: {}", id);

        return systemMapper.toSkillResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        log.info("Deleting skill with ID: {}", id);
        if (!skillRepository.existsById(id)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        skillRepository.deleteById(id);
        log.info("Deleted skill with ID: {}", id);
    }

    @Override
    public Page<SkillResponse> getAll(Pageable pageable) {
        log.debug("Fetching all skills with pagination");
        return skillRepository.findAll(pageable)
                .map(systemMapper::toSkillResponse);
    }

    @Override
    public List<SkillResponse> getAllActive() {
        log.debug("Fetching all active skills");
        return skillRepository.findByIsActiveTrueOrderByLabelEnAsc().stream()
                .map(systemMapper::toSkillResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SkillResponse> searchByName(String query) {
        log.debug("Searching skills with query: {}", query);
        return skillRepository.findByLabelEnContainingIgnoreCaseOrLabelViContainingIgnoreCase(query, query).stream()
                .map(systemMapper::toSkillResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void toggleActive(Integer id) {
        log.info("Toggling active status for skill with ID: {}", id);
        Skill entity = skillRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        entity.setIsActive(!entity.getIsActive());
        skillRepository.save(entity);
        log.info("Toggled active status for skill with ID: {}", id);
    }

    @Override
    public long count() {
        return skillRepository.count();
    }
}
