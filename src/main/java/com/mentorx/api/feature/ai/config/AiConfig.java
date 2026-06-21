package com.mentorx.api.feature.ai.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OpenRouterConfig.class)
public class AiConfig {
}
