package com.mentorx.api.common.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class FptAiConfig {

    @Value("${app.fpt-ai.api-key:cfZhosADkpTSwn2veN6XaoZQKAQpPmnx}")
    private String apiKey;

    @Value("${app.fpt-ai.ocr-url:https://api.fpt.ai/vision/idr/vport}")
    private String ocrUrl;

    @Value("${app.fpt-ai.face-match-url:https://api.fpt.ai/vision/face/match}")
    private String faceMatchUrl;
}
