package com.mentorx.api.feature.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for user activity
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityResponse {
    
    /**
     * Number of active courses
     */
    private Integer activeCourses;
    
    /**
     * Number of active job contracts
     */
    private Integer activeContracts;
    
    /**
     * Number of in-progress learning items
     */
    private Integer inProgressItems;
    
    /**
     * List of recent activities
     */
    private List<ActivityItem> recentActivities;
    
    /**
     * Individual activity item
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityItem {
        private String type;
        private String title;
        private String description;
        private String timestamp;
    }
}
