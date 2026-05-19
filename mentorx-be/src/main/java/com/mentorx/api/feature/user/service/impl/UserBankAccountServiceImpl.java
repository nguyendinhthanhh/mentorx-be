package com.mentorx.api.feature.user.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.user.dto.request.BankAccountRequest;
import com.mentorx.api.feature.user.dto.response.BankAccountResponse;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.entity.UserBankAccount;
import com.mentorx.api.feature.user.repository.UserBankAccountRepository;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.user.service.UserBankAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBankAccountServiceImpl implements UserBankAccountService {

    private final UserBankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BankAccountResponse create(UUID userId, BankAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Check if account number already exists for this user
        if (bankAccountRepository.existsByUserIdAndAccountNumber(userId, request.accountNumber())) {
            throw new AppException(ErrorCode.BANK_ACCOUNT_ALREADY_EXISTS);
        }

        // If this is set as default or it's the first account, clear other defaults
        if (Boolean.TRUE.equals(request.isDefault()) || bankAccountRepository.countByUserId(userId) == 0) {
            bankAccountRepository.clearDefaultForUser(userId);
        }

        UserBankAccount bankAccount = UserBankAccount.builder()
                .user(user)
                .bankName(request.bankName())
                .bankCode(request.bankCode())
                .accountNumber(request.accountNumber())
                .accountHolderName(request.accountHolderName())
                .branchName(request.branchName())
                .isDefault(Boolean.TRUE.equals(request.isDefault()) || bankAccountRepository.countByUserId(userId) == 0)
                .isVerified(false)
                .notes(request.notes())
                .build();

        bankAccount = bankAccountRepository.save(bankAccount);
        log.info("Created bank account {} for user {}", bankAccount.getId(), userId);

        return toResponse(bankAccount);
    }

    @Override
    @Transactional
    public BankAccountResponse update(UUID id, UUID userId, BankAccountRequest request) {
        UserBankAccount bankAccount = bankAccountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AppException(ErrorCode.BANK_ACCOUNT_NOT_FOUND));

        // Check if account number is being changed and if it already exists
        if (!bankAccount.getAccountNumber().equals(request.accountNumber()) &&
                bankAccountRepository.existsByUserIdAndAccountNumber(userId, request.accountNumber())) {
            throw new AppException(ErrorCode.BANK_ACCOUNT_ALREADY_EXISTS);
        }

        // If setting as default, clear other defaults
        if (Boolean.TRUE.equals(request.isDefault()) && !bankAccount.getIsDefault()) {
            bankAccountRepository.clearDefaultForUser(userId);
        }

        bankAccount.setBankName(request.bankName());
        bankAccount.setBankCode(request.bankCode());
        bankAccount.setAccountNumber(request.accountNumber());
        bankAccount.setAccountHolderName(request.accountHolderName());
        bankAccount.setBranchName(request.branchName());
        bankAccount.setIsDefault(Boolean.TRUE.equals(request.isDefault()));
        bankAccount.setNotes(request.notes());

        // Reset verification if account details changed
        if (!bankAccount.getAccountNumber().equals(request.accountNumber()) ||
                !bankAccount.getAccountHolderName().equals(request.accountHolderName())) {
            bankAccount.setIsVerified(false);
            bankAccount.setVerifiedAt(null);
            bankAccount.setVerifiedBy(null);
        }

        bankAccount = bankAccountRepository.save(bankAccount);
        log.info("Updated bank account {} for user {}", id, userId);

        return toResponse(bankAccount);
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID userId) {
        UserBankAccount bankAccount = bankAccountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AppException(ErrorCode.BANK_ACCOUNT_NOT_FOUND));

        boolean wasDefault = bankAccount.getIsDefault();
        bankAccountRepository.delete(bankAccount);
        log.info("Deleted bank account {} for user {}", id, userId);

        // If deleted account was default, set another account as default
        if (wasDefault) {
            List<UserBankAccount> remainingAccounts = bankAccountRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
            if (!remainingAccounts.isEmpty()) {
                UserBankAccount newDefault = remainingAccounts.get(0);
                newDefault.setIsDefault(true);
                bankAccountRepository.save(newDefault);
            }
        }
    }

    @Override
    public BankAccountResponse getById(UUID id, UUID userId) {
        UserBankAccount bankAccount = bankAccountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AppException(ErrorCode.BANK_ACCOUNT_NOT_FOUND));
        return toResponse(bankAccount);
    }

    @Override
    public List<BankAccountResponse> getByUserId(UUID userId) {
        return bankAccountRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BankAccountResponse getDefaultAccount(UUID userId) {
        UserBankAccount bankAccount = bankAccountRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new AppException(ErrorCode.DEFAULT_BANK_ACCOUNT_NOT_FOUND));
        return toResponse(bankAccount);
    }

    @Override
    @Transactional
    public BankAccountResponse setAsDefault(UUID id, UUID userId) {
        UserBankAccount bankAccount = bankAccountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AppException(ErrorCode.BANK_ACCOUNT_NOT_FOUND));

        // Clear other defaults
        bankAccountRepository.clearDefaultForUser(userId);

        // Set this as default
        bankAccount.setIsDefault(true);
        bankAccount = bankAccountRepository.save(bankAccount);
        log.info("Set bank account {} as default for user {}", id, userId);

        return toResponse(bankAccount);
    }

    @Override
    @Transactional
    public BankAccountResponse verifyAccount(UUID id, String verifiedBy) {
        UserBankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BANK_ACCOUNT_NOT_FOUND));

        bankAccount.setIsVerified(true);
        bankAccount.setVerifiedAt(LocalDateTime.now());
        bankAccount.setVerifiedBy(verifiedBy);

        bankAccount = bankAccountRepository.save(bankAccount);
        log.info("Verified bank account {} by {}", id, verifiedBy);

        return toResponse(bankAccount);
    }

    @Override
    public long countByUserId(UUID userId) {
        return bankAccountRepository.countByUserId(userId);
    }

    private BankAccountResponse toResponse(UserBankAccount bankAccount) {
        return BankAccountResponse.builder()
                .id(bankAccount.getId())
                .userId(bankAccount.getUser().getId())
                .bankName(bankAccount.getBankName())
                .bankCode(bankAccount.getBankCode())
                .accountNumber(bankAccount.getAccountNumber())
                .accountHolderName(bankAccount.getAccountHolderName())
                .branchName(bankAccount.getBranchName())
                .isDefault(bankAccount.getIsDefault())
                .isVerified(bankAccount.getIsVerified())
                .verifiedAt(bankAccount.getVerifiedAt())
                .verifiedBy(bankAccount.getVerifiedBy())
                .notes(bankAccount.getNotes())
                .createdAt(bankAccount.getCreatedAt())
                .updatedAt(bankAccount.getUpdatedAt())
                .build();
    }
}
