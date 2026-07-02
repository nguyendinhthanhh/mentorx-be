package com.mentorx.api.feature.blog.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.blog.dto.BlogPostDto;
import com.mentorx.api.feature.blog.enums.BlogAudience;
import com.mentorx.api.feature.blog.enums.BlogCategory;
import com.mentorx.api.feature.blog.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BlogPostDto>>> searchPosts(
            @RequestParam(required = false) BlogAudience audience,
            @RequestParam(required = false) BlogCategory category,
            @RequestParam(required = false) String query,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        return ResponseEntity.ok(ApiResponse.success(blogService.searchPosts(audience, category, query, pageable)));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<BlogPostDto>>> getFeaturedPosts() {
        return ResponseEntity.ok(ApiResponse.success(blogService.getFeaturedPosts()));
    }
}
