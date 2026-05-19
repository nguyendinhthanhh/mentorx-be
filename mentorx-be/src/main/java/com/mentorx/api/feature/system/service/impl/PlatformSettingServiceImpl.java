package com.mentorx.api.feature.system.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.system.dto.request.PlatformSettingRequest;
import com.mentorx.api.feature.system.dto.response.PlatformSettingResponse;
import com.mentorx.api.feature.system.entity.PlatformSetting;
import com.mentorx.api.feature.system.mapper.SystemMapper;
import com.mentorx.api.feature.system.repository.PlatformSettingRepository;
import com.mentorx.api.feature.system.service.PlatformSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class PlatformSettingServiceImpl implements PlatformSettingService {

    private final PlatformSettingRepository platformSettingRepository;
    private final SystemMapper systemMapper;

    @Override
    @Transactional
    public PlatformSettingResponse create(PlatformSettingRequest request) {
        log.info("Creating platform setting with key: {}", request.key());

        if (platformSettingRepository.existsById(request.key())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Platform setting with this key already exists");
        }

        PlatformSetting entity = systemMapper.toPlatformSetting(request);
        entity.setUpdatedAt(LocalDateTime.now());

        PlatformSetting saved = platformSettingRepository.save(entity);
        log.info("Created platform setting with key: {}", saved.getKey());

        return systemMapper.toPlatformSettingResponse(saved);
    }

    @Override
    public PlatformSettingResponse getByKey(String key) {
        log.debug("Fetching platform setting with key: {}", key);
        PlatformSetting entity = platformSettingRepository.findById(key)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return systemMapper.toPlatformSettingResponse(entity);
    }

    @Override
    @Transactional
    public PlatformSettingResponse update(String key, PlatformSettingRequest request) {
        log.info("Updating platform setting with key: {}", key);

        PlatformSetting entity = platformSettingRepository.findById(key)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        systemMapper.updatePlatformSetting(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());

        PlatformSetting updated = platformSettingRepository.save(entity);
        log.info("Updated platform setting with key: {}", key);

        return systemMapper.toPlatformSettingResponse(updated);
    }

    @Override
    @Transactional
    public void delete(String key) {
        log.info("Deleting platform setting with key: {}", key);
        if (!platformSettingRepository.existsById(key)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        platformSettingRepository.deleteById(key);
        log.info("Deleted platform setting with key: {}", key);
    }

    @Override
    public Page<PlatformSettingResponse> getAll(Pageable pageable) {
        log.debug("Fetching all platform settings with pagination");
        return platformSettingRepository.findAll(pageable)
                .map(systemMapper::toPlatformSettingResponse);
    }

    @Override
    public List<PlatformSettingResponse> getAll() {
        log.debug("Fetching all platform settings");
        return platformSettingRepository.findAll().stream()
                .map(systemMapper::toPlatformSettingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public String getValue(String key) {
        log.debug("Fetching value for platform setting key: {}", key);
        PlatformSetting entity = platformSettingRepository.findById(key)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return entity.getValue();
    }

    @Override
    @Transactional
    public void updateValue(String key, String value, UUID updatedBy) {
        log.info("Updating value for platform setting key: {}", key);
        PlatformSetting entity = platformSettingRepository.findById(key)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        
        entity.setValue(value);
        entity.setUpdatedBy(updatedBy);
        entity.setUpdatedAt(LocalDateTime.now());
        
        platformSettingRepository.save(entity);
        log.info("Updated value for platform setting key: {}", key);
    }
}
