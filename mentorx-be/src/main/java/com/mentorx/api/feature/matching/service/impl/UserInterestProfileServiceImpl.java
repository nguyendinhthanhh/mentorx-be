package com.mentorx.api.feature.matching.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.matching.dto.request.UserInterestProfileRequest;
import com.mentorx.api.feature.matching.dto.response.UserInterestProfileResponse;
import com.mentorx.api.feature.matching.entity.UserInterestProfile;
import com.mentorx.api.feature.matching.mapper.MatchingMapper;
import com.mentorx.api.feature.matching.repository.UserInterestProfileRepository;
import com.mentorx.api.feature.matching.service.UserInterestProfileService;
import com.mentorx.api.feature.system.entity.Category;
import com.mentorx.api.feature.system.repository.CategoryRepository;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserInterestProfileServiceImpl implements UserInterestProfileService {

    private final UserInterestProfileRepository userInterestProfileRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final MatchingMapper matchingMapper;

    @Override
    @Transactional
    public UserInterestProfileResponse create(UserInterestProfileRequest request) {
        log.info("Creating user interest profile for user {} and category {}", request.userId(), request.categoryId());

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        UserInterestProfile entity = matchingMapper.toUserInterestProfile(request);
        entity.setUser(user);
        entity.setCategory(category);
        entity.setLastUpdated(LocalDateTime.now());

        if (entity.getInteractionCount() == null) {
            entity.setInteractionCount(0);
        }
        if (entity.getTimeSpentMinutes() == null) {
            entity.setTimeSpentMinutes(0);
        }
        if (entity.getDecayFactor() == null) {
            entity.setDecayFactor(new BigDecimal("0.9500"));
        }
        if (entity.getIsExplicit() == null) {
            entity.setIsExplicit(false);
        }

        UserInterestProfile saved = userInterestProfileRepository.save(entity);
        log.info("Created user interest profile with ID: {}", saved.getId());

        return matchingMapper.toUserInterestProfileResponse(saved);
    }

    @Override
    public UserInterestProfileResponse getById(UUID id) {
        log.debug("Fetching user interest profile with ID: {}", id);
        UserInterestProfile entity = userInterestProfileRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return matchingMapper.toUserInterestProfileResponse(entity);
    }

    @Override
    @Transactional
    public UserInterestProfileResponse update(UUID id, UserInterestProfileRequest request) {
        log.info("Updating user interest profile with ID: {}", id);

        UserInterestProfile entity = userInterestProfileRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (request.userId() != null && !entity.getUser().getId().equals(request.userId())) {
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            entity.setUser(user);
        }

        if (request.categoryId() != null && !entity.getCategory().getId().equals(request.categoryId())) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            entity.setCategory(category);
        }

        matchingMapper.updateUserInterestProfile(entity, request);
        entity.setLastUpdated(LocalDateTime.now());

        UserInterestProfile updated = userInterestProfileRepository.save(entity);
        log.info("Updated user interest profile with ID: {}", id);

        return matchingMapper.toUserInterestProfileResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting user interest profile with ID: {}", id);
        if (!userInterestProfileRepository.existsById(id)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        userInterestProfileRepository.deleteById(id);
        log.info("Deleted user interest profile with ID: {}", id);
    }

    @Override
    public Page<UserInterestProfileResponse> getAll(Pageable pageable) {
        log.debug("Fetching all user interest profiles with pagination");
        return userInterestProfileRepository.findAll(pageable)
                .map(matchingMapper::toUserInterestProfileResponse);
    }

    @Override
    public List<UserInterestProfileResponse> getByUserId(UUID userId) {
        log.debug("Fetching user interest profiles for user: {}", userId);
        return userInterestProfileRepository.findByUserIdOrderByInterestScoreDesc(userId).stream()
                .map(matchingMapper::toUserInterestProfileResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserInterestProfileResponse> getTopInterestsForUser(UUID userId, int limit) {
        log.debug("Fetching top {} interests for user: {}", limit, userId);
        return userInterestProfileRepository.findByUserIdOrderByInterestScoreDesc(userId).stream()
                .limit(limit)
                .map(matchingMapper::toUserInterestProfileResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void recordInteraction(UUID userId, Integer categoryId, int timeSpentMinutes) {
        log.debug("Recording interaction for user {} in category {}", userId, categoryId);

        UserInterestProfile profile = userInterestProfileRepository
                .findByUserIdAndCategoryId(userId, categoryId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                    Category category = categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

                    UserInterestProfile newProfile = new UserInterestProfile();
                    newProfile.setUser(user);
                    newProfile.setCategory(category);
                    newProfile.setInterestScore(BigDecimal.ZERO);
                    newProfile.setInteractionCount(0);
                    newProfile.setTimeSpentMinutes(0);
                    newProfile.setDecayFactor(new BigDecimal("0.9500"));
                    newProfile.setIsExplicit(false);
                    newProfile.setLastUpdated(LocalDateTime.now());
                    return newProfile;
                });

        profile.setInteractionCount(profile.getInteractionCount() + 1);
        profile.setTimeSpentMinutes(profile.getTimeSpentMinutes() + timeSpentMinutes);
        profile.setLastInteractionAt(LocalDateTime.now());
        profile.setLastUpdated(LocalDateTime.now());

        // Simple interest score calculation: normalize by max interactions
        BigDecimal newScore = BigDecimal.valueOf(profile.getInteractionCount())
                .divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
        if (newScore.compareTo(BigDecimal.ONE) > 0) {
            newScore = BigDecimal.ONE;
        }
        profile.setInterestScore(newScore);

        userInterestProfileRepository.save(profile);
        log.debug("Recorded interaction for user {} in category {}", userId, categoryId);
    }

    @Override
    @Transactional
    public void applyDecay(UUID userId) {
        log.info("Applying decay to interest profiles for user: {}", userId);
        List<UserInterestProfile> profiles = userInterestProfileRepository.findByUserIdOrderByInterestScoreDesc(userId);

        List<UUID> profileIds = profiles.stream()
                .map(UserInterestProfile::getId)
                .collect(Collectors.toList());

        if (!profileIds.isEmpty()) {
            userInterestProfileRepository.applyDecayToProfiles(profileIds, LocalDateTime.now());
            log.info("Applied decay to {} interest profiles for user: {}", profileIds.size(), userId);
        }
    }
}
