package com.mentorx.api.feature.user.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("vnpt-ekyc")
@EnableConfigurationProperties(VnptEkycProperties.class)
public class VnptEkycConfiguration {

    @Bean
    RestTemplate vnptRestTemplate(VnptEkycProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.connectTimeoutMs());
        factory.setReadTimeout(properties.readTimeoutMs());
        return new RestTemplate(factory);
    }
}
