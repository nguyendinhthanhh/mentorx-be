package com.mentorx.api.feature.home.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeStatsResponse {
    private long users;
    private long openJobs;
    private long mentors;
    private long successfulMatches;
}
