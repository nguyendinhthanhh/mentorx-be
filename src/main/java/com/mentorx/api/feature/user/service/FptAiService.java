package com.mentorx.api.feature.user.service;

import java.util.Map;

public interface FptAiService {
    Map<String, Object> ocrIdCard(byte[] imageBytes);
    Map<String, Object> faceMatch(byte[] image1Bytes, byte[] image2Bytes);
}
