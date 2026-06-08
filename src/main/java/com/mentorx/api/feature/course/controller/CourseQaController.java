package com.mentorx.api.feature.course.controller;

import com.mentorx.api.common.util.SecurityUtils;
import com.mentorx.api.feature.course.dto.request.CourseQaMessageRequest;
import com.mentorx.api.feature.course.dto.response.CourseQaMessageResponse;
import com.mentorx.api.feature.course.service.CourseQaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/course-qa")
@RequiredArgsConstructor
public class CourseQaController {

    private final CourseQaService courseQaService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/courses/{courseId}/messages")
    @PreAuthorize("isAuthenticated()")
    public List<CourseQaMessageResponse> recent(@PathVariable UUID courseId) {
        return courseQaService.recent(courseId);
    }

    @PostMapping("/courses/{courseId}/messages")
    @PreAuthorize("isAuthenticated()")
    public CourseQaMessageResponse sendRest(
            @PathVariable UUID courseId,
            @Valid @RequestBody CourseQaMessageRequest request) {
        CourseQaMessageResponse response = courseQaService.send(courseId, SecurityUtils.getCurrentUserId(), request);
        messagingTemplate.convertAndSend("/topic/course-qa/" + courseId, response);
        return response;
    }

    @MessageMapping("/course-qa/{courseId}/send")
    public void sendSocket(@DestinationVariable UUID courseId, @Valid @Payload CourseQaMessageRequest request) {
        CourseQaMessageResponse response = courseQaService.send(courseId, SecurityUtils.getCurrentUserId(), request);
        messagingTemplate.convertAndSend("/topic/course-qa/" + courseId, response);
    }
}
