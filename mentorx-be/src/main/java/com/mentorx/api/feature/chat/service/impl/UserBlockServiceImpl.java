package com.mentorx.api.feature.chat.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.chat.dto.request.UserBlockRequest;
import com.mentorx.api.feature.chat.dto.response.UserBlockResponse;
import com.mentorx.api.feature.chat.entity.UserBlock;
import com.mentorx.api.feature.chat.repository.UserBlockRepository;
import com.mentorx.api.feature.chat.service.UserBlockService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBlockServiceImpl implements UserBlockService {

    private final UserBlockRepository userBlockRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserBlockResponse blockUser(UserBlockRequest request) {
        User blocker = userRepository.findById(request.blockerUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        User blocked = userRepository.findById(request.blockedUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        UserBlock userBlock = userBlockRepository
                .findByBlockerUserIdAndBlockedUserId(blocker.getId(), blocked.getId())
                .orElse(new UserBlock());

        userBlock.setBlockerUser(blocker);
        userBlock.setBlockedUser(blocked);
        userBlock.setBlockReason(request.blockReason());
        userBlock.setBlockType(request.blockType() != null ? request.blockType() : "ALL");
        userBlock.setIsActive(true);

        if (request.isTemporary() != null && request.isTemporary()) {
            userBlock.setIsTemporary(true);
            userBlock.setExpiresAt(LocalDateTime.now().plusHours(request.durationHours() != null ? request.durationHours() : 24));
        } else {
            userBlock.makePermanent();
        }

        return toResponse(userBlockRepository.save(userBlock));
    }

    @Override
    @Transactional
    public UserBlockResponse unblockUser(UUID blockId, String reason) {
        UserBlock block = userBlockRepository.findById(blockId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND)); 

        block.unblock(reason);
        return toResponse(userBlockRepository.save(block));
    }

    private UserBlockResponse toResponse(UserBlock block) {
        return new UserBlockResponse(
                block.getId(),
                block.getBlockerUser().getId(),
                block.getBlockedUser().getId(),
                block.getBlockedAt(),
                block.getIsActive(),
                block.getBlockReason(),
                block.getBlockType(),
                block.getIsTemporary(),
                block.getExpiresAt(),
                block.getCreatedAt(),
                block.getUpdatedAt()
        );
    }
}
