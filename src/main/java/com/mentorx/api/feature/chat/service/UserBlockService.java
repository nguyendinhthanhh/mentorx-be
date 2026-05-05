package com.mentorx.api.feature.chat.service;

import com.mentorx.api.feature.chat.dto.request.UserBlockRequest;
import com.mentorx.api.feature.chat.dto.response.UserBlockResponse;

import java.util.UUID;

public interface UserBlockService {
    UserBlockResponse blockUser(UserBlockRequest request);
    UserBlockResponse unblockUser(UUID blockId, String reason);
}
