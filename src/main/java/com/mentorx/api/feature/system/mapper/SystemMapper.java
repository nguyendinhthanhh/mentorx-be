package com.mentorx.api.feature.system.mapper;

import com.mentorx.api.feature.system.dto.request.*;
import com.mentorx.api.feature.system.dto.response.*;
import com.mentorx.api.feature.system.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SystemMapper {

    // Category mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Category toCategory(CategoryRequest request);

    @Mapping(target = "categoryId", source = "id")
    @Mapping(target = "parentCategoryId", source = "parentId")
    @Mapping(target = "name", source = "labelEn") // Default to English name for the name field
    @Mapping(target = "parentName", expression = "java(getParentCategoryName(entity))")
    CategoryResponse toCategoryResponse(Category entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateCategory(@MappingTarget Category entity, CategoryRequest request);

    default String getParentCategoryName(Category entity) {
        // This will be implemented in service layer if needed
        return null;
    }

    // Skill mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Skill toSkill(SkillRequest request);

    SkillResponse toSkillResponse(Skill entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateSkill(@MappingTarget Skill entity, SkillRequest request);

    // PlatformSetting mappings
    @Mapping(target = "updatedAt", ignore = true)
    PlatformSetting toPlatformSetting(PlatformSettingRequest request);

    @Mapping(target = "updatedByName", expression = "java(getUpdatedByName(entity))")
    PlatformSettingResponse toPlatformSettingResponse(PlatformSetting entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "updatedAt", ignore = true)
    void updatePlatformSetting(@MappingTarget PlatformSetting entity, PlatformSettingRequest request);

    default String getUpdatedByName(PlatformSetting entity) {
        // This will be implemented in service layer if needed
        return null;
    }

    // Permission mappings
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updatePermission(@MappingTarget Permission entity, PermissionRequest request);

    // NotificationPreference mappings
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    NotificationPreference toNotificationPreference(NotificationPreferenceRequest request);

    @Mapping(target = "userFullName", expression = "java(getUserFullName(entity))")
    NotificationPreferenceResponse toNotificationPreferenceResponse(NotificationPreference entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateNotificationPreference(@MappingTarget NotificationPreference entity, NotificationPreferenceRequest request);

    default String getUserFullName(NotificationPreference entity) {
        // This will be implemented in service layer if needed
        return null;
    }
}
