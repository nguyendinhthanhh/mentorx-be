package com.mentorx.api.feature.user.service.impl;

import com.mentorx.api.common.enums.VerificationStatus;
import com.mentorx.api.common.enums.PayoutMethod;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.security.MentorModeAccessService;
import com.mentorx.api.feature.user.entity.MentorProfile;
import com.mentorx.api.feature.user.dto.request.BankAccountRequest;
import com.mentorx.api.feature.user.dto.response.BankAccountResponse;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.entity.UserBankAccount;
import com.mentorx.api.feature.user.repository.MentorProfileRepository;
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
    private final MentorProfileRepository mentorProfileRepository;
    private final MentorModeAccessService mentorModeAccessService;

    @Override
    @Transactional
    public BankAccountResponse create(UUID userId, BankAccountRequest request) {
        mentorModeAccessService.requireSelfOrAdmin(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        validatePayoutRequest(request);

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
                .payoutCountry(request.payoutCountry())
                .payoutMethod(request.payoutMethod())
                .iban(request.iban())
                .swiftCode(request.swiftCode())
                .paypalEmail(request.paypalEmail())
                .wiseEmail(request.wiseEmail())
                .stripeConnectAccountId(request.stripeConnectAccountId())
                .isDefault(Boolean.TRUE.equals(request.isDefault()) || bankAccountRepository.countByUserId(userId) == 0)
                .isVerified(false)
                .notes(request.notes())
                .build();

        bankAccount = bankAccountRepository.save(bankAccount);
        upsertPayoutStatus(user, VerificationStatus.PENDING, null);
        log.info("Created bank account {} for user {}", bankAccount.getId(), userId);

        return toResponse(bankAccount);
    }

    @Override
    @Transactional
    public BankAccountResponse update(UUID id, UUID userId, BankAccountRequest request) {
        mentorModeAccessService.requireSelfOrAdmin(userId);
        UserBankAccount bankAccount = bankAccountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AppException(ErrorCode.BANK_ACCOUNT_NOT_FOUND));
        validatePayoutRequest(request);

        // Check if account number is being changed and if it already exists
        if (!bankAccount.getAccountNumber().equals(request.accountNumber()) &&
                bankAccountRepository.existsByUserIdAndAccountNumber(userId, request.accountNumber())) {
            throw new AppException(ErrorCode.BANK_ACCOUNT_ALREADY_EXISTS);
        }

        // If setting as default, clear other defaults
        if (Boolean.TRUE.equals(request.isDefault()) && !bankAccount.getIsDefault()) {
            bankAccountRepository.clearDefaultForUser(userId);
        }

        boolean shouldResetVerification =
                !bankAccount.getAccountNumber().equals(request.accountNumber()) ||
                !bankAccount.getAccountHolderName().equals(request.accountHolderName()) ||
                !safeEquals(bankAccount.getPayoutMethod(), request.payoutMethod()) ||
                !safeEquals(bankAccount.getPayoutCountry(), request.payoutCountry());

        bankAccount.setBankName(request.bankName());
        bankAccount.setBankCode(request.bankCode());
        bankAccount.setAccountNumber(request.accountNumber());
        bankAccount.setAccountHolderName(request.accountHolderName());
        bankAccount.setBranchName(request.branchName());
        bankAccount.setPayoutCountry(request.payoutCountry());
        bankAccount.setPayoutMethod(request.payoutMethod());
        bankAccount.setIban(request.iban());
        bankAccount.setSwiftCode(request.swiftCode());
        bankAccount.setPaypalEmail(request.paypalEmail());
        bankAccount.setWiseEmail(request.wiseEmail());
        bankAccount.setStripeConnectAccountId(request.stripeConnectAccountId());
        bankAccount.setIsDefault(Boolean.TRUE.equals(request.isDefault()));
        bankAccount.setNotes(request.notes());

        if (shouldResetVerification) {
            bankAccount.setIsVerified(false);
            bankAccount.setVerifiedAt(null);
            bankAccount.setVerifiedBy(null);
        }

        bankAccount = bankAccountRepository.save(bankAccount);
        upsertPayoutStatus(bankAccount.getUser(), VerificationStatus.PENDING, null);
        log.info("Updated bank account {} for user {}", id, userId);

        return toResponse(bankAccount);
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID userId) {
        mentorModeAccessService.requireSelfOrAdmin(userId);
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
            } else {
                upsertPayoutStatus(bankAccount.getUser(), VerificationStatus.NOT_SUBMITTED, null);
            }
        }
    }

    @Override
    public BankAccountResponse getById(UUID id, UUID userId) {
        mentorModeAccessService.requireSelfOrAdmin(userId);
        UserBankAccount bankAccount = bankAccountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AppException(ErrorCode.BANK_ACCOUNT_NOT_FOUND));
        return toResponse(bankAccount);
    }

    @Override
    public List<BankAccountResponse> getByUserId(UUID userId) {
        mentorModeAccessService.requireSelfOrAdmin(userId);
        return bankAccountRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BankAccountResponse getDefaultAccount(UUID userId) {
        mentorModeAccessService.requireSelfOrAdmin(userId);
        UserBankAccount bankAccount = bankAccountRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new AppException(ErrorCode.DEFAULT_BANK_ACCOUNT_NOT_FOUND));
        return toResponse(bankAccount);
    }

    @Override
    @Transactional
    public BankAccountResponse setAsDefault(UUID id, UUID userId) {
        mentorModeAccessService.requireSelfOrAdmin(userId);
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
        upsertPayoutStatus(bankAccount.getUser(), VerificationStatus.APPROVED, null);
        log.info("Verified bank account {} by {}", id, verifiedBy);

        return toResponse(bankAccount);
    }

    @Override
    @Transactional
    public BankAccountResponse rejectAccount(UUID id, String rejectedBy, String reason) {
        UserBankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BANK_ACCOUNT_NOT_FOUND));

        bankAccount.setIsVerified(false);
        bankAccount.setVerifiedAt(null);
        bankAccount.setVerifiedBy(null);
        bankAccount.setNotes(reason);
        bankAccount = bankAccountRepository.save(bankAccount);
        upsertPayoutStatus(bankAccount.getUser(), VerificationStatus.REJECTED, reason);
        log.info("Rejected payout account {} by {}", id, rejectedBy);
        return toResponse(bankAccount);
    }

    @Override
    public long countByUserId(UUID userId) {
        mentorModeAccessService.requireSelfOrAdmin(userId);
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
                .payoutCountry(bankAccount.getPayoutCountry())
                .payoutMethod(bankAccount.getPayoutMethod())
                .iban(bankAccount.getIban())
                .swiftCode(bankAccount.getSwiftCode())
                .paypalEmail(bankAccount.getPaypalEmail())
                .wiseEmail(bankAccount.getWiseEmail())
                .stripeConnectAccountId(bankAccount.getStripeConnectAccountId())
                .isDefault(bankAccount.getIsDefault())
                .isVerified(bankAccount.getIsVerified())
                .verifiedAt(bankAccount.getVerifiedAt())
                .verifiedBy(bankAccount.getVerifiedBy())
                .notes(bankAccount.getNotes())
                .createdAt(bankAccount.getCreatedAt())
                .updatedAt(bankAccount.getUpdatedAt())
                .build();
    }

    private void upsertPayoutStatus(User user, VerificationStatus status, String rejectionReason) {
        MentorProfile profile = mentorProfileRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null) {
            return;
        }
        profile.setPayoutStatus(status);
        profile.setPayoutRejectionReason(rejectionReason);
        if (status == VerificationStatus.APPROVED) {
            profile.setPayoutReviewedAt(LocalDateTime.now());
        } else if (status == VerificationStatus.PENDING) {
            profile.setPayoutReviewedAt(null);
        }
        mentorProfileRepository.save(profile);
    }

    private boolean safeEquals(Object left, Object right) {
        return left == null ? right == null : left.equals(right);
    }

    private void validatePayoutRequest(BankAccountRequest request) {
        PayoutMethod payoutMethod = request.payoutMethod() == null ? PayoutMethod.LOCAL_BANK : request.payoutMethod();

        switch (payoutMethod) {
            case LOCAL_BANK -> {
                requireText(request.bankName(), "Bank name is required for local bank payout.");
                requireText(request.accountNumber(), "Account number is required for local bank payout.");
            }
            case INTERNATIONAL_BANK -> {
                requireText(request.bankName(), "Bank name is required for international bank payout.");
                if (isBlank(request.accountNumber()) && isBlank(request.iban())) {
                    throw new AppException(ErrorCode.BAD_REQUEST, "Account number or IBAN is required for international bank payout.");
                }
                requireText(request.swiftCode(), "SWIFT code is required for international bank payout.");
            }
            case PAYPAL -> requireText(request.paypalEmail(), "PayPal email is required for PayPal payout.");
            case WISE -> requireText(request.wiseEmail(), "Wise email is required for Wise payout.");
            case STRIPE_CONNECT -> requireText(request.stripeConnectAccountId(), "Stripe Connect account ID is required for Stripe Connect payout.");
        }
    }

    private void requireText(String value, String message) {
        if (isBlank(value)) {
            throw new AppException(ErrorCode.BAD_REQUEST, message);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
