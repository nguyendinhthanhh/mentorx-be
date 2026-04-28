package com.mentorx.api.feature.mentor.mapper;

import com.mentorx.api.feature.mentor.dto.request.MentorApplicationRequest;
import com.mentorx.api.feature.mentor.dto.request.MentorAvailabilityRequest;
import com.mentorx.api.feature.mentor.dto.request.MentorCertificationRequest;
import com.mentorx.api.feature.mentor.dto.request.MentorSkillRequest;
import com.mentorx.api.feature.mentor.dto.response.MentorAvailabilityResponse;
import com.mentorx.api.feature.mentor.dto.response.MentorCertificationResponse;
import com.mentorx.api.feature.mentor.dto.response.MentorProfileResponse;
import com.mentorx.api.feature.mentor.dto.response.MentorSkillResponse;
import com.mentorx.api.feature.mentor.entity.MentorAvailability;
import com.mentorx.api.feature.mentor.entity.MentorCertification;
import com.mentorx.api.feature.mentor.entity.MentorProfile;
import com.mentorx.api.feature.mentor.entity.MentorSkill;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MentorMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "adminNotes", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectedAt", ignore = true)
    @Mapping(target = "mentorSkills", source = "skills")
    @Mapping(target = "mentorAvailabilities", source = "availabilities")
    @Mapping(target = "mentorCertifications", source = "certifications")
    MentorProfile toMentorProfile(MentorApplicationRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "userDisplayName", source = "user.displayName")
    @Mapping(target = "userAvatarUrl", source = "user.avatarUrl")
    @Mapping(target = "approvedBy", source = "approvedBy.id")
    @Mapping(target = "approvedByName", source = "approvedBy.fullName")
    @Mapping(target = "skills", source = "mentorSkills")
    @Mapping(target = "availabilities", source = "mentorAvailabilities")
    @Mapping(target = "certifications", source = "mentorCertifications")
    MentorProfileResponse toMentorProfileResponse(MentorProfile mentorProfile);

    List<MentorProfileResponse> toMentorProfileResponseList(List<MentorProfile> mentorProfiles);

    @Mapping(target = "mentorProfile", ignore = true)
    MentorSkill toMentorSkill(MentorSkillRequest request);

    MentorSkillResponse toMentorSkillResponse(MentorSkill mentorSkill);

    List<MentorSkillResponse> toMentorSkillResponseList(List<MentorSkill> mentorSkills);

    @Mapping(target = "mentorProfile", ignore = true)
    MentorAvailability toMentorAvailability(MentorAvailabilityRequest request);

    MentorAvailabilityResponse toMentorAvailabilityResponse(MentorAvailability mentorAvailability);

    List<MentorAvailabilityResponse> toMentorAvailabilityResponseList(List<MentorAvailability> mentorAvailabilities);

    @Mapping(target = "mentorProfile", ignore = true)
    @Mapping(target = "isVerified", ignore = true)
    MentorCertification toMentorCertification(MentorCertificationRequest request);

    MentorCertificationResponse toMentorCertificationResponse(MentorCertification mentorCertification);

    List<MentorCertificationResponse> toMentorCertificationResponseList(List<MentorCertification> mentorCertifications);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "adminNotes", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectedAt", ignore = true)
    @Mapping(target = "mentorSkills", ignore = true)
    @Mapping(target = "mentorAvailabilities", ignore = true)
    @Mapping(target = "mentorCertifications", ignore = true)
    void updateMentorProfile(@MappingTarget MentorProfile mentorProfile, MentorApplicationRequest request);
}