package com.mentorx.api.feature.payment.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PayOSProperties.class)
public class PayOSConfiguration {
}
