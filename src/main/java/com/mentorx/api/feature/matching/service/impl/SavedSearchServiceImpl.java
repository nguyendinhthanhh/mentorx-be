package com.mentorx.api.feature.matching.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.matching.dto.request.SavedSearchRequest;
import com.mentorx.api.feature.matching.dto.response.SavedSearchResponse;
import com.mentorx.api.feature.matching.entity.SavedSearch;
import com.mentorx.api.feature.matching.mapper.MatchingMapper;
import com.mentorx.api.feature.matching.repository.SavedSearchRepository;
import com.mentorx.api.feature.matching.service.SavedSearchService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SavedSearchServiceImpl implements SavedSearchService {

    private final SavedSearchRepository savedSearchRepository;
    private final UserRepository userRepository;
    private final MatchingMapper matchingMapper;

    @Override
    @Transactional
    public SavedSearchResponse create(SavedSearchRequest request) {
        log.info("Creating saved search for user {}: {}", request.userId(), request.name());

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (savedSearchRepository.existsByUserIdAndName(request.userId(), request.name())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Saved search with this name already exists");
        }

        SavedSearch entity = matchingMapper.toSavedSearch(request);
        entity.setUser(user);

        SavedSearch saved = savedSearchRepository.save(entity);
        log.info("Created saved search with ID: {}", saved.getId());

        return matchingMapper.toSavedSearchResponse(saved);
    }

    @Override
    public SavedSearchResponse getById(UUID id) {
        log.debug("Fetching saved search with ID: {}", id);
        SavedSearch entity = savedSearchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return matchingMapper.toSavedSearchResponse(entity);
    }

    @Override
    @Transactional
    public SavedSearchResponse update(UUID id, SavedSearchRequest request) {
        log.info("Updating saved search with ID: {}", id);

        SavedSearch entity = savedSearchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // Check if name is being changed and if it conflicts
        if (request.name() != null && !request.name().equals(entity.getName())) {
            if (savedSearchRepository.existsByUserIdAndName(entity.getUser().getId(), request.name())) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Saved search with this name already exists");
            }
        }

        matchingMapper.updateSavedSearch(entity, request);

        SavedSearch updated = savedSearchRepository.save(entity);
        log.info("Updated saved search with ID: {}", id);

        return matchingMapper.toSavedSearchResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting saved search with ID: {}", id);
        if (!savedSearchRepository.existsById(id)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        savedSearchRepository.deleteById(id);
        log.info("Deleted saved search with ID: {}", id);
    }

    @Override
    public Page<SavedSearchResponse> getAll(Pageable pageable) {
        log.debug("Fetching all saved searches with pagination");
        return savedSearchRepository.findAll(pageable)
                .map(matchingMapper::toSavedSearchResponse);
    }

    @Override
    public List<SavedSearchResponse> getByUserId(UUID userId) {
        log.debug("Fetching saved searches for user: {}", userId);
        return savedSearchRepository.findByUserId(userId).stream()
                .map(matchingMapper::toSavedSearchResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<SavedSearchResponse> getByUserIdPaginated(UUID userId, Pageable pageable) {
        log.debug("Fetching saved searches for user {} with pagination", userId);
        return savedSearchRepository.findByUserId(userId, pageable)
                .map(matchingMapper::toSavedSearchResponse);
    }

    @Override
    public long countByUserId(UUID userId) {
        log.debug("Counting saved searches for user: {}", userId);
        return savedSearchRepository.countByUserId(userId);
    }
}
