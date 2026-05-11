package com.mentorx.api.feature.user.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.user.service.EkycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user/ekyc")
@RequiredArgsConstructor
@Tag(name = "eKYC", description = "Identity Verification APIs")
@Slf4j
public class EkycController {

    private final EkycService ekycService;

    @PostMapping(value = "/verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Verify user identity using CCCD and Selfie")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyIdentity(
            @RequestParam("frontImage") MultipartFile frontImage,
            @RequestParam("backImage") MultipartFile backImage,
            @RequestParam("selfieImage") MultipartFile selfieImage) {
        
        log.info("Received eKYC verification request");
        ApiResponse<Map<String, Object>> response = ekycService.verifyIdentity(frontImage, backImage, selfieImage);
        
        return ResponseEntity.ok(response);
    }
}
