package com.mentorx.api.feature.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.file")
@Data
public class FileStorageProperties {
    private String uploadDir;
}
