package com.mentorx.api.feature.system.service;

import com.mentorx.api.feature.system.dto.request.CategoryRequest;
import com.mentorx.api.feature.system.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    
    CategoryResponse create(CategoryRequest request);
    
    CategoryResponse getById(Integer id);
    
    CategoryResponse getBySlug(String slug);
    
    CategoryResponse update(Integer id, CategoryRequest request);
    
    void delete(Integer id);
    
    Page<CategoryResponse> getAll(Pageable pageable);
    
    List<CategoryResponse> getAllActive();
    
    List<CategoryResponse> getByParentId(Integer parentId);
    
    List<CategoryResponse> getRootCategories();
    
    void toggleActive(Integer id);
    
    void updateDisplayOrder(Integer id, Short displayOrder);
}
