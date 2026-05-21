package com.mentorx.api.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubUser(
    Long id,
    String login,
    String name,
    String email
) {}
