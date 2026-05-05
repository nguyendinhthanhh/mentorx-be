package com.mentorx.api.feature.matching.mapper;

import com.mentorx.api.feature.matching.dto.request.MentorMatchScoreRequest;
import com.mentorx.api.feature.matching.dto.request.SavedSearchRequest;
import com.mentorx.api.feature.matching.dto.request.UserInterestProfileRequest;
import com.mentorx.api.feature.matching.dto.response.MentorMatchScoreResponse;
import com.mentorx.api.feature.matching.dto.response.SavedSearchResponse;
import com.mentorx.api.feature.matching.dto.response.UserInterestProfileResponse;
import com.mentorx.api.feature.matching.entity.MentorMatchScore;
import com.mentorx.api.feature.matching.entity.SavedSearch;
import com.mentorx.api.feature.matching.entity.UserInterestProfile;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MatchingMapper {

    // MentorMatchScore mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "mentorProfile", ignore = true)
    @Mapping(target = "computedAt", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "isShown", constant = "false")
    @Mapping(target = "showCount", constant = "0")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MentorMatchScore toMentorMatchScore(MentorMatchScoreRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "mentorProfileId", source = "mentorProfile.id")
    @Mapping(target = "mentorFullName", source = "mentorProfile.user.fullName")
    MentorMatchScoreResponse toMentorMatchScoreResponse(MentorMatchScore entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "mentorProfile", ignore = true)
    @Mapping(target = "computedAt", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateMentorMatchScore(@MappingTarget MentorMatchScore entity, MentorMatchScoreRequest request);

    // UserInterestProfile mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "lastInteractionAt", ignore = true)
    @Mapping(target = "lastUpdated", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserInterestProfile toUserInterestProfile(UserInterestProfileRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.labelEn")
    UserInterestProfileResponse toUserInterestProfileResponse(UserInterestProfile entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "lastInteractionAt", ignore = true)
    @Mapping(target = "lastUpdated", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateUserInterestProfile(@MappingTarget UserInterestProfile entity, UserInterestProfileRequest request);

    // SavedSearch mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    SavedSearch toSavedSearch(SavedSearchRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFullName", source = "user.fullName")
    SavedSearchResponse toSavedSearchResponse(SavedSearch entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateSavedSearch(@MappingTarget SavedSearch entity, SavedSearchRequest request);
}
