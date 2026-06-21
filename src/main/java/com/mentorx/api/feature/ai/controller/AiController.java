package com.mentorx.api.feature.ai.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.ai.dto.request.ExplainTaskRequest;
import com.mentorx.api.feature.ai.dto.response.ExplainTaskResponse;
import com.mentorx.api.feature.ai.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/explain")
    public ApiResponse<ExplainTaskResponse> explainTask(
            @Valid @RequestBody ExplainTaskRequest request
    ) {
        return ApiResponse.success(aiService.explainTask(request));
    }
}
