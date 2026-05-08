package com.mentorx.api.feature.home.service.impl;

import com.mentorx.api.feature.home.dto.response.HomeDataResponse;
import com.mentorx.api.feature.home.dto.response.HomeStatsResponse;
import com.mentorx.api.feature.home.service.HomeService;
import com.mentorx.api.feature.job.dto.response.JobResponse;
import com.mentorx.api.feature.job.service.JobService;
import com.mentorx.api.feature.system.dto.response.CategoryResponse;
import com.mentorx.api.feature.system.service.CategoryService;
import com.mentorx.api.feature.user.dto.response.MentorProfileResponse;
import com.mentorx.api.feature.user.service.MentorProfileService;
import com.mentorx.api.feature.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final JobService jobService;
    private final MentorProfileService mentorProfileService;
    private final CategoryService categoryService;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public HomeDataResponse getHomeData() {
        // Fetch 8 open jobs
        Page<JobResponse> jobsPage = jobService.getOpenJobs(null, null, PageRequest.of(0, 8));
        List<JobResponse> featuredJobs = jobsPage.getContent();

        // Fetch featured mentors
        List<MentorProfileResponse> featuredMentors = mentorProfileService.getFeaturedMentors();
        if (featuredMentors == null || featuredMentors.isEmpty()) {
            Page<MentorProfileResponse> mentorsPage = mentorProfileService.getAllApprovedMentors(PageRequest.of(0, 4));
            featuredMentors = mentorsPage.getContent();
        } else if (featuredMentors.size() > 4) {
            featuredMentors = featuredMentors.subList(0, 4);
        }

        // Fetch categories
        List<CategoryResponse> categories = categoryService.getAllActive();
        if (categories != null && categories.size() > 8) {
            categories = categories.subList(0, 8);
        }

        // Stats
        long openJobsCount = jobsPage.getTotalElements();
        long usersCount = userService.getActiveUsersCount();
        long mentorsCount = userService.getMentorsCount();

        HomeStatsResponse stats = HomeStatsResponse.builder()
                .users(usersCount)
                .openJobs(openJobsCount)
                .mentors(mentorsCount)
                .successfulMatches(45000) // Hardcoded for now
                .build();

        return HomeDataResponse.builder()
                .featuredJobs(featuredJobs)
                .featuredMentors(featuredMentors)
                .categories(categories)
                .stats(stats)
                .build();
    }
}
