package com.mentorx.api.feature.home.dto.response;

import com.mentorx.api.feature.job.dto.response.JobResponse;
import com.mentorx.api.feature.system.dto.response.CategoryResponse;
import com.mentorx.api.feature.user.dto.response.MentorProfileResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeDataResponse {
    private List<JobResponse> featuredJobs;
    private List<MentorProfileResponse> featuredMentors;
    private List<CategoryResponse> categories;
    private HomeStatsResponse stats;
}
