package com.mentorx.api.feature.matching.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.matching.dto.request.MentorMatchScoreRequest;
import com.mentorx.api.feature.matching.dto.response.MentorMatchScoreResponse;
import com.mentorx.api.feature.matching.entity.MentorMatchScore;
import com.mentorx.api.feature.matching.mapper.MatchingMapper;
import com.mentorx.api.feature.matching.repository.MentorMatchScoreRepository;
import com.mentorx.api.feature.matching.service.MentorMatchScoreService;
import com.mentorx.api.feature.user.entity.MentorProfile;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.MentorProfileRepository;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MentorMatchScoreServiceImpl implements MentorMatchScoreService {

    private final MentorMatchScoreRepository mentorMatchScoreRepository;
    private final UserRepository userRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final MatchingMapper matchingMapper;

    @Override
    @Transactional
    public MentorMatchScoreResponse create(MentorMatchScoreRequest request) {
        log.info("Creating mentor match score for user {} and mentor {}", request.userId(), request.mentorProfileId());

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        MentorProfile mentorProfile = mentorProfileRepository.findById(request.mentorProfileId())
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));

        MentorMatchScore entity = matchingMapper.toMentorMatchScore(request);
        entity.setUser(user);
        entity.setMentorProfile(mentorProfile);
        entity.setComputedAt(LocalDateTime.now());
        entity.setExpiresAt(LocalDateTime.now().plusDays(7));

        MentorMatchScore saved = mentorMatchScoreRepository.save(entity);
        log.info("Created mentor match score with ID: {}", saved.getId());

        return matchingMapper.toMentorMatchScoreResponse(saved);
    }

    @Override
    public MentorMatchScoreResponse getById(UUID id) {
        log.debug("Fetching mentor match score with ID: {}", id);
        MentorMatchScore entity = mentorMatchScoreRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return matchingMapper.toMentorMatchScoreResponse(entity);
    }

    @Override
    @Transactional
    public MentorMatchScoreResponse update(UUID id, MentorMatchScoreRequest request) {
        log.info("Updating mentor match score with ID: {}", id);

        MentorMatchScore entity = mentorMatchScoreRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (request.userId() != null && !entity.getUser().getId().equals(request.userId())) {
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            entity.setUser(user);
        }

        if (request.mentorProfileId() != null && !entity.getMentorProfile().getId().equals(request.mentorProfileId())) {
            MentorProfile mentorProfile = mentorProfileRepository.findById(request.mentorProfileId())
                    .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));
            entity.setMentorProfile(mentorProfile);
        }

        matchingMapper.updateMentorMatchScore(entity, request);
        entity.setComputedAt(LocalDateTime.now());
        entity.setExpiresAt(LocalDateTime.now().plusDays(7));

        MentorMatchScore updated = mentorMatchScoreRepository.save(entity);
        log.info("Updated mentor match score with ID: {}", id);

        return matchingMapper.toMentorMatchScoreResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting mentor match score with ID: {}", id);
        if (!mentorMatchScoreRepository.existsById(id)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        mentorMatchScoreRepository.deleteById(id);
        log.info("Deleted mentor match score with ID: {}", id);
    }

    @Override
    public Page<MentorMatchScoreResponse> getAll(Pageable pageable) {
        log.debug("Fetching all mentor match scores with pagination");
        return mentorMatchScoreRepository.findAll(pageable)
                .map(matchingMapper::toMentorMatchScoreResponse);
    }

    @Override
    public Page<MentorMatchScoreResponse> getByUserId(UUID userId, Pageable pageable) {
        log.debug("Fetching mentor match scores for user: {}", userId);
        return mentorMatchScoreRepository.findTopMatchesForUser(userId, LocalDateTime.now(), pageable)
                .map(matchingMapper::toMentorMatchScoreResponse);
    }

    @Override
    public Page<MentorMatchScoreResponse> getByMentorProfileId(UUID mentorProfileId, Pageable pageable) {
        log.debug("Fetching mentor match scores for mentor profile: {}", mentorProfileId);
        return mentorMatchScoreRepository.findMatchesForMentor(mentorProfileId, LocalDateTime.now(), pageable)
                .map(matchingMapper::toMentorMatchScoreResponse);
    }

    @Override
    public List<MentorMatchScoreResponse> getTopMatchesForUser(UUID userId, int limit) {
        log.debug("Fetching top {} matches for user: {}", limit, userId);
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "matchScore"));
        return mentorMatchScoreRepository.findTopMatchesForUser(userId, LocalDateTime.now(), pageable)
                .stream()
                .map(matchingMapper::toMentorMatchScoreResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsShown(UUID id) {
        log.debug("Marking mentor match score as shown: {}", id);
        MentorMatchScore entity = mentorMatchScoreRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!entity.getIsShown()) {
            entity.setIsShown(true);
            entity.setShownAt(LocalDateTime.now());
        }
        entity.setShowCount(entity.getShowCount() + 1);
        mentorMatchScoreRepository.save(entity);
    }

    @Override
    @Transactional
    public void recomputeExpiredScores() {
        log.info("Recomputing expired mentor match scores");
        LocalDateTime now = LocalDateTime.now();
        List<MentorMatchScore> expiredScores = mentorMatchScoreRepository.findExpiredMatches(now);
        
        log.info("Found {} expired scores to recompute", expiredScores.size());
        
        for (MentorMatchScore score : expiredScores) {
            // TODO: Implement actual matching algorithm
            score.setComputedAt(now);
            score.setExpiresAt(now.plusDays(7));
            mentorMatchScoreRepository.save(score);
        }
        
        log.info("Recomputed {} expired scores", expiredScores.size());
    }

    @Override
    @Transactional
    public void computeMatchScore(UUID userId, UUID mentorProfileId) {
        log.info("Computing match score for user {} and mentor {}", userId, mentorProfileId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        MentorProfile mentorProfile = mentorProfileRepository.findById(mentorProfileId)
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));

        // TODO: Implement actual matching algorithm
        // For now, create a placeholder score
        MentorMatchScore score = new MentorMatchScore();
        score.setUser(user);
        score.setMentorProfile(mentorProfile);
        score.setMatchScore(java.math.BigDecimal.valueOf(0.75));
        score.setComputedAt(LocalDateTime.now());
        score.setExpiresAt(LocalDateTime.now().plusDays(7));
        score.setAlgorithmVersion("1.0.0");
        
        mentorMatchScoreRepository.save(score);
        log.info("Computed match score for user {} and mentor {}", userId, mentorProfileId);
    }
}
