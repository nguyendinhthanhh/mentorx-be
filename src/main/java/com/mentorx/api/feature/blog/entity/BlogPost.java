package com.mentorx.api.feature.blog.entity;

import com.mentorx.api.common.entity.BaseEntity;
import com.mentorx.api.feature.blog.enums.BlogAudience;
import com.mentorx.api.feature.blog.enums.BlogCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blog_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogPost extends BaseEntity {

    @Column(nullable = false, length = 300, unique = true)
    private String slug;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String excerpt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlogCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlogAudience audience;

    @Column(length = 100)
    private String author;

    @Column(name = "author_role", length = 100)
    private String authorRole;

    @Column(name = "author_avatar", length = 500)
    private String authorAvatar;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "read_time", length = 50)
    private String readTime;

    @Builder.Default
    @Column(nullable = false)
    private Boolean featured = false;

    @ElementCollection
    @CollectionTable(name = "blog_post_tags", joinColumns = @JoinColumn(name = "blog_post_id"))
    @Column(name = "tag", length = 100)
    @Builder.Default
    private List<String> tags = new ArrayList<>();
}
