package com.mentorx.api.feature.user.service;

import com.mentorx.api.feature.user.dto.request.BankAccountRequest;
import com.mentorx.api.feature.user.dto.response.BankAccountResponse;

import java.util.List;
import java.util.UUID;

public interface UserBankAccountService {

    BankAccountResponse create(UUID userId, BankAccountRequest request);

    BankAccountResponse update(UUID id, UUID userId, BankAccountRequest request);

    void delete(UUID id, UUID userId);

    BankAccountResponse getById(UUID id, UUID userId);

    List<BankAccountResponse> getByUserId(UUID userId);

    BankAccountResponse getDefaultAccount(UUID userId);

    BankAccountResponse setAsDefault(UUID id, UUID userId);

    BankAccountResponse verifyAccount(UUID id, String verifiedBy);

    BankAccountResponse rejectAccount(UUID id, String rejectedBy, String reason);

    long countByUserId(UUID userId);
}
