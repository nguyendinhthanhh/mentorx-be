package com.mentorx.api.feature.wallet.mapper;

import com.mentorx.api.feature.wallet.dto.response.DepositOrderResponse;
import com.mentorx.api.feature.wallet.dto.response.EscrowRecordResponse;
import com.mentorx.api.feature.wallet.dto.response.WalletResponse;
import com.mentorx.api.feature.wallet.dto.response.WalletTransactionResponse;
import com.mentorx.api.feature.wallet.dto.response.WithdrawalResponse;
import com.mentorx.api.feature.wallet.entity.DepositOrder;
import com.mentorx.api.feature.wallet.entity.EscrowRecord;
import com.mentorx.api.feature.wallet.entity.Wallet;
import com.mentorx.api.feature.wallet.entity.WalletTransaction;
import com.mentorx.api.feature.wallet.entity.WithdrawalRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFullName", source = "user.fullName")
    WalletResponse toWalletResponse(Wallet wallet);

    List<WalletResponse> toWalletResponseList(List<Wallet> wallets);

    @Mapping(target = "walletId", source = "wallet.id")
    WalletTransactionResponse toWalletTransactionResponse(WalletTransaction transaction);

    List<WalletTransactionResponse> toWalletTransactionResponseList(List<WalletTransaction> transactions);

    @Mapping(target = "contractId", source = "contract.id")
    @Mapping(target = "milestoneId", source = "milestone.id")
    @Mapping(target = "releasedToUserId", source = "releasedTo.id")
    @Mapping(target = "releasedToFullName", source = "releasedTo.fullName")
    EscrowRecordResponse toEscrowRecordResponse(EscrowRecord escrowRecord);

    List<EscrowRecordResponse> toEscrowRecordResponseList(List<EscrowRecord> escrowRecords);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "gateway", source = "gateway")
    DepositOrderResponse toDepositOrderResponse(DepositOrder depositOrder);

    @Mapping(target = "userId", source = "user.id")
    WithdrawalResponse toWithdrawalResponse(WithdrawalRequest withdrawalRequest);

    List<WithdrawalResponse> toWithdrawalResponseList(List<WithdrawalRequest> withdrawalRequests);
}
