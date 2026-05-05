package com.mentorx.api.feature.course.service.impl;

import com.mentorx.api.common.exception.ErrorCode;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.feature.course.dto.request.LessonCommentCreateRequest;
import com.mentorx.api.feature.course.dto.request.LessonCommentUpdateRequest;
import com.mentorx.api.feature.course.dto.response.LessonCommentResponse;
import com.mentorx.api.feature.course.entity.CourseLesson;
import com.mentorx.api.feature.course.entity.LessonComment;
import com.mentorx.api.feature.course.mapper.CourseMapper;
import com.mentorx.api.feature.course.repository.CourseLessonRepository;
import com.mentorx.api.feature.course.repository.LessonCommentRepository;
import com.mentorx.api.feature.course.service.LessonCommentService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
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
public class LessonCommentServiceImpl implements LessonCommentService {

    private final LessonCommentRepository commentRepository;
    private final CourseLessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final CourseMapper mapper;

    @Override
    @Transactional
    public LessonCommentResponse createComment(LessonCommentCreateRequest request, UUID userId) {
        log.info("Creating comment for lesson: {} by user: {}", request.getLessonId(), userId);

        CourseLesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        LessonComment comment = mapper.toEntity(request);
        comment.setLesson(lesson);
        comment.setUser(user);
        comment.setIsDeleted(false);

        if (request.getParentId() != null) {
            LessonComment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
            comment.setParent(parent);
        }

        LessonComment savedComment = commentRepository.save(comment);
        log.info("Comment created successfully with ID: {}", savedComment.getId());

        return mapper.toResponse(savedComment);
    }

    @Override
    public LessonCommentResponse getCommentById(UUID id) {
        log.debug("Fetching comment by ID: {}", id);
        
        LessonComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        if (comment.getIsDeleted()) {
            throw new AppException(ErrorCode.COMMENT_DELETED);
        }

        LessonCommentResponse response = mapper.toResponse(comment);
        
        // Load replies
        List<LessonComment> replies = commentRepository.findByParentIdAndIsDeletedFalse(id);
        response.setReplies(mapper.toCommentResponseList(replies));

        return response;
    }

    @Override
    public Page<LessonCommentResponse> getCommentsByLessonId(UUID lessonId, Pageable pageable) {
        log.debug("Fetching comments for lesson: {}", lessonId);
        
        Page<LessonComment> comments = commentRepository.findByLessonIdAndParentIsNullAndIsDeletedFalse(lessonId, pageable);
        return comments.map(comment -> {
            LessonCommentResponse response = mapper.toResponse(comment);
            List<LessonComment> replies = commentRepository.findByParentIdAndIsDeletedFalse(comment.getId());
            response.setReplies(mapper.toCommentResponseList(replies));
            return response;
        });
    }

    @Override
    public List<LessonCommentResponse> getRepliesByParentId(UUID parentId) {
        log.debug("Fetching replies for comment: {}", parentId);
        
        List<LessonComment> replies = commentRepository.findByParentIdAndIsDeletedFalse(parentId);
        return mapper.toCommentResponseList(replies);
    }

    @Override
    public Page<LessonCommentResponse> getCommentsByUserId(UUID userId, Pageable pageable) {
        log.debug("Fetching comments by user: {}", userId);
        
        Page<LessonComment> comments = commentRepository.findByUserId(userId, pageable);
        return comments.map(mapper::toResponse);
    }

    @Override
    @Transactional
    public LessonCommentResponse updateComment(UUID id, LessonCommentUpdateRequest request, UUID userId) {
        log.info("Updating comment: {} by user: {}", id, userId);

        LessonComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.COMMENT_UPDATE_FORBIDDEN);
        }

        if (comment.getIsDeleted()) {
            throw new AppException(ErrorCode.COMMENT_DELETED);
        }

        mapper.updateEntity(request, comment);
        LessonComment updatedComment = commentRepository.save(comment);
        
        log.info("Comment updated successfully: {}", id);
        return mapper.toResponse(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(UUID id, UUID userId) {
        log.info("Deleting comment: {} by user: {}", id, userId);

        LessonComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.COMMENT_DELETE_FORBIDDEN);
        }

        comment.setIsDeleted(true);
        commentRepository.save(comment);
        
        log.info("Comment marked as deleted: {}", id);
    }

    @Override
    public Long countCommentsByLessonId(UUID lessonId) {
        log.debug("Counting comments for lesson: {}", lessonId);
        return commentRepository.countByLessonId(lessonId);
    }
}
