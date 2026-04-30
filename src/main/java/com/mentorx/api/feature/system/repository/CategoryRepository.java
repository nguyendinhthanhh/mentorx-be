package com.mentorx.api.feature.system.repository;

import com.mentorx.api.feature.system.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findBySlug(String slug);
    List<Category> findByParentId(Integer parentId);
    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();
}
