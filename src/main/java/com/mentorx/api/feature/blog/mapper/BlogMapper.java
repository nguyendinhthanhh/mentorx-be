package com.mentorx.api.feature.blog.mapper;

import com.mentorx.api.feature.blog.dto.BlogPostDto;
import com.mentorx.api.feature.blog.entity.BlogPost;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BlogMapper {

    BlogPostDto toDto(BlogPost entity);

    List<BlogPostDto> toDtoList(List<BlogPost> entities);
}
