package com.mentorx.api.feature.wallet.service.impl;

import com.mentorx.api.common.enums.EscrowStatus;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.job.entity.Contract;
import com.mentorx.api.feature.job.entity.Milestone;
import com.mentorx.api.feature.job.repository.ContractRepository;
import com.mentorx.api.feature.job.repository.MilestoneRepository;
import com.mentorx.api.feature.wallet.dto.response.EscrowRecordResponse;
import com.mentorx.api.feature.wallet.entity.EscrowRecord;
import com.mentorx.api.feature.wallet.mapper.WalletMapper;
import com.mentorx.api.feature.wallet.repository.EscrowRecordRepository;
import com.mentorx.api.feature.wallet.service.EscrowService;
import com.mentorx.api.feature.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EscrowServiceImpl implements EscrowService {

    private final EscrowRecordRepository escrowRecordRepository;
    private final ContractRepository contractRepository;
    private final MilestoneRepository milestoneRepository;
    private final WalletService walletService;
    private final WalletMapper walletMapper;

    @Override
    @Transactional
    public EscrowRecordResponse lockEscrowForContract(UUID clientId, UUID contractId,
                                                       BigDecimal totalAmountMxc, BigDecimal platformFeeMxc) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        BigDecimal mentorNetMxc = totalAmountMxc.subtract(platformFeeMxc);

        // 1. Tạo escrow record
        EscrowRecord escrowRecord = EscrowRecord.builder()
                .contract(contract)
                .lockedAmountMxc(totalAmountMxc)
                .platformFeeMxc(platformFeeMxc)
                .mentorNetMxc(mentorNetMxc)
                .status(EscrowStatus.LOCKED)
                .build();
        escrowRecord = escrowRecordRepository.save(escrowRecord);

        // 2. Lock tiền từ CLIENT_USER_AVAILABLE → ESCROW (wallet ledger)
        walletService.processJobPayment(clientId, contractId, totalAmountMxc);

        log.info("Escrow locked: contractId={}, amount={}, fee={}, mentorNet={}",
                contractId, totalAmountMxc, platformFeeMxc, mentorNetMxc);

        return walletMapper.toEscrowRecordResponse(escrowRecord);
    }

    @Override
    @Transactional
    public EscrowRecordResponse releaseMilestoneEscrow(UUID contractId, UUID milestoneId,
                                                        BigDecimal milestoneAmount, BigDecimal platformFee,
                                                        UUID mentorId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new AppException(ErrorCode.MILESTONE_NOT_FOUND));

        BigDecimal mentorNet = milestoneAmount.subtract(platformFee);

        // Tạo escrow record cho milestone
        EscrowRecord escrowRecord = EscrowRecord.builder()
                .contract(contract)
                .milestone(milestone)
                .lockedAmountMxc(milestoneAmount)
                .platformFeeMxc(platformFee)
                .mentorNetMxc(mentorNet)
                .status(EscrowStatus.RELEASED)
                .releasedAt(LocalDateTime.now())
                .build();
        escrowRecord = escrowRecordRepository.save(escrowRecord);

        // Release: ESCROW → PLATFORM_REVENUE (fee) + MENTOR_USER_PENDING (net)
        walletService.releaseMilestone(contractId, milestoneId, milestoneAmount, platformFee, mentorId);

        log.info("Milestone released: contractId={}, milestoneId={}, amount={}, fee={}, mentorNet={}",
                contractId, milestoneId, milestoneAmount, platformFee, mentorNet);

        return walletMapper.toEscrowRecordResponse(escrowRecord);
    }

    @Override
    @Transactional
    public EscrowRecordResponse refundEscrow(UUID contractId, UUID clientId, BigDecimal refundAmount) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        // Tìm escrow record chưa release (LOCKED)
        EscrowRecord escrowRecord = escrowRecordRepository.findByContractIdAndMilestoneIdIsNull(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.ESCROW_NOT_FOUND));

        if (escrowRecord.getStatus() == EscrowStatus.REFUNDED) {
            throw new AppException(ErrorCode.ESCROW_ALREADY_REFUNDED);
        }

        // Refund: ESCROW → CLIENT_USER_AVAILABLE
        walletService.processRefund(contractId, clientId, refundAmount);

        escrowRecord.setStatus(EscrowStatus.REFUNDED);
        escrowRecord.setReleasedAt(LocalDateTime.now());
        escrowRecordRepository.save(escrowRecord);

        log.info("Escrow refunded: contractId={}, clientId={}, amount={}", contractId, clientId, refundAmount);

        return walletMapper.toEscrowRecordResponse(escrowRecord);
    }

    @Override
    public List<EscrowRecordResponse> getEscrowsByContract(UUID contractId) {
        List<EscrowRecord> records = escrowRecordRepository.findByContractId(contractId);
        return walletMapper.toEscrowRecordResponseList(records);
    }

    @Override
    public EscrowRecordResponse getEscrowById(UUID escrowId) {
        EscrowRecord record = escrowRecordRepository.findById(escrowId)
                .orElseThrow(() -> new AppException(ErrorCode.ESCROW_NOT_FOUND));
        return walletMapper.toEscrowRecordResponse(record);
    }

    @Override
    public BigDecimal getTotalLockedByContract(UUID contractId) {
        return escrowRecordRepository.getTotalLockedByContract(contractId);
    }

    @Override
    public BigDecimal getTotalReleasedByContract(UUID contractId) {
        return escrowRecordRepository.getTotalReleasedByContract(contractId);
    }

    @Override
    public BigDecimal getTotalEscrowLocked() {
        return escrowRecordRepository.getTotalEscrowLocked();
    }
}
