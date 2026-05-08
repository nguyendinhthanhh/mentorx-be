package com.mentorx.api.feature.home.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.home.dto.response.HomeDataResponse;
import com.mentorx.api.feature.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    public ResponseEntity<ApiResponse<HomeDataResponse>> getHomeData() {
        HomeDataResponse data = homeService.getHomeData();
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
