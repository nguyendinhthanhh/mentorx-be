package com.mentorx.api.feature.blog.service;

import com.mentorx.api.feature.blog.dto.BlogPostDto;
import com.mentorx.api.feature.blog.enums.BlogAudience;
import com.mentorx.api.feature.blog.enums.BlogCategory;
import com.mentorx.api.feature.blog.mapper.BlogMapper;
import com.mentorx.api.feature.blog.repository.BlogPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogPostRepository blogPostRepository;
    private final BlogMapper blogMapper;

    @Transactional(readOnly = true)
    public Page<BlogPostDto> searchPosts(BlogAudience audience, BlogCategory category, String query, Pageable pageable) {
        return blogPostRepository.searchPosts(audience, category, query, pageable)
                .map(blogMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<BlogPostDto> getFeaturedPosts() {
        return blogMapper.toDtoList(blogPostRepository.findByFeaturedTrueOrderByCreatedAtDesc());
    }
}
