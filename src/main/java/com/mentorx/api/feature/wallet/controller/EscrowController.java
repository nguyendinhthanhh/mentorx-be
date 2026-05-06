package com.mentorx.api.feature.wallet.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.wallet.dto.response.EscrowRecordResponse;
import com.mentorx.api.feature.wallet.service.EscrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallet/escrow")
@RequiredArgsConstructor
public class EscrowController {

    private final EscrowService escrowService;

    /**
     * Lấy tất cả escrow records của 1 contract
     */
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<ApiResponse<List<EscrowRecordResponse>>> getEscrowsByContract(
            @PathVariable UUID contractId) {
        return ResponseEntity.ok(ApiResponse.success(escrowService.getEscrowsByContract(contractId)));
    }

    /**
     * Lấy chi tiết 1 escrow record
     */
    @GetMapping("/{escrowId}")
    public ResponseEntity<ApiResponse<EscrowRecordResponse>> getEscrowById(@PathVariable UUID escrowId) {
        return ResponseEntity.ok(ApiResponse.success(escrowService.getEscrowById(escrowId)));
    }

    /**
     * Tổng tiền đang lock trong escrow cho 1 contract
     */
    @GetMapping("/contract/{contractId}/locked")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalLockedByContract(@PathVariable UUID contractId) {
        return ResponseEntity.ok(ApiResponse.success(escrowService.getTotalLockedByContract(contractId)));
    }

    /**
     * Tổng tiền đã release cho 1 contract
     */
    @GetMapping("/contract/{contractId}/released")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalReleasedByContract(@PathVariable UUID contractId) {
        return ResponseEntity.ok(ApiResponse.success(escrowService.getTotalReleasedByContract(contractId)));
    }

    /**
     * Tổng tiền đang bị lock toàn hệ thống (Admin)
     */
    @GetMapping("/total-locked")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalEscrowLocked() {
        return ResponseEntity.ok(ApiResponse.success(escrowService.getTotalEscrowLocked()));
    }
}
