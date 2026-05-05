package com.mentorx.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MentorxBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(MentorxBeApplication.class, args);
    }

}
