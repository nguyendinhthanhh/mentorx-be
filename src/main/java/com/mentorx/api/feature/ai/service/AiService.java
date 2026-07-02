package com.mentorx.api.feature.ai.service;

import com.mentorx.api.feature.ai.dto.request.ExplainTaskRequest;
import com.mentorx.api.feature.ai.dto.response.ExplainTaskResponse;

public interface AiService {
    ExplainTaskResponse explainTask(ExplainTaskRequest request);
}
