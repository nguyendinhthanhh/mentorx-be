package com.mentorx.api.feature.system.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.system.dto.request.CategoryRequest;
import com.mentorx.api.feature.system.dto.response.CategoryResponse;
import com.mentorx.api.feature.system.entity.Category;
import com.mentorx.api.feature.system.mapper.SystemMapper;
import com.mentorx.api.feature.system.repository.CategoryRepository;
import com.mentorx.api.feature.system.service.CategoryService;
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
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final SystemMapper systemMapper;

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        log.info("Creating category with slug: {}", request.slug());

        if (categoryRepository.existsBySlug(request.slug())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Category with this slug already exists");
        }

        Category entity = systemMapper.toCategory(request);
        entity.setCreatedAt(LocalDateTime.now());
        
        if (entity.getIsActive() == null) {
            entity.setIsActive(true);
        }
        if (entity.getDisplayOrder() == null) {
            entity.setDisplayOrder((short) 0);
        }

        Category saved = categoryRepository.save(entity);
        log.info("Created category with ID: {}", saved.getId());

        return systemMapper.toCategoryResponse(saved);
    }

    @Override
    public CategoryResponse getById(Integer id) {
        log.debug("Fetching category with ID: {}", id);
        Category entity = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return systemMapper.toCategoryResponse(entity);
    }

    @Override
    public CategoryResponse getBySlug(String slug) {
        log.debug("Fetching category with slug: {}", slug);
        Category entity = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return systemMapper.toCategoryResponse(entity);
    }

    @Override
    @Transactional
    public CategoryResponse update(Integer id, CategoryRequest request) {
        log.info("Updating category with ID: {}", id);

        Category entity = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // Check slug uniqueness if changed
        if (request.slug() != null && !request.slug().equals(entity.getSlug())) {
            if (categoryRepository.existsBySlug(request.slug())) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Category with this slug already exists");
            }
        }

        systemMapper.updateCategory(entity, request);

        Category updated = categoryRepository.save(entity);
        log.info("Updated category with ID: {}", id);

        return systemMapper.toCategoryResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        log.info("Deleting category with ID: {}", id);
        if (!categoryRepository.existsById(id)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        categoryRepository.deleteById(id);
        log.info("Deleted category with ID: {}", id);
    }

    @Override
    public Page<CategoryResponse> getAll(Pageable pageable) {
        log.debug("Fetching all categories with pagination");
        return categoryRepository.findAll(pageable)
                .map(systemMapper::toCategoryResponse);
    }

    @Override
    public List<CategoryResponse> getAllActive() {
        log.debug("Fetching all active categories");
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(systemMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getByParentId(Integer parentId) {
        log.debug("Fetching categories with parent ID: {}", parentId);
        return categoryRepository.findByParentIdOrderByDisplayOrderAsc(parentId).stream()
                .map(systemMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getRootCategories() {
        log.debug("Fetching root categories");
        return categoryRepository.findByParentIdIsNullOrderByDisplayOrderAsc().stream()
                .map(systemMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void toggleActive(Integer id) {
        log.info("Toggling active status for category with ID: {}", id);
        Category entity = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        entity.setIsActive(!entity.getIsActive());
        categoryRepository.save(entity);
        log.info("Toggled active status for category with ID: {}", id);
    }

    @Override
    @Transactional
    public void updateDisplayOrder(Integer id, Short displayOrder) {
        log.info("Updating display order for category with ID: {}", id);
        Category entity = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        entity.setDisplayOrder(displayOrder);
        categoryRepository.save(entity);
        log.info("Updated display order for category with ID: {}", id);
    }
}
