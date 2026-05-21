package com.mentorx.api.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubEmail(
    String email,
    boolean primary,
    boolean verified
) {}
