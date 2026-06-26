package com.mentorx.api.feature.blog.repository;

import com.mentorx.api.feature.blog.entity.BlogPost;
import com.mentorx.api.feature.blog.enums.BlogAudience;
import com.mentorx.api.feature.blog.enums.BlogCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, UUID> {

    Optional<BlogPost> findBySlug(String slug);

    List<BlogPost> findByFeaturedTrueOrderByCreatedAtDesc();

    @Query("SELECT b FROM BlogPost b WHERE " +
           "(:audience IS NULL OR b.audience = :audience) AND " +
           "(:category IS NULL OR b.category = :category) AND " +
           "(:query IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(b.excerpt) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<BlogPost> searchPosts(
            @Param("audience") BlogAudience audience,
            @Param("category") BlogCategory category,
            @Param("query") String query,
            Pageable pageable);
}
