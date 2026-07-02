package com.mentorx.api.feature.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.openrouter")
public record OpenRouterConfig(
    String apiKey,
    String model,
    String url
) {}
