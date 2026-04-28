package com.mentorx.api.feature.wallet.mapper;

import com.mentorx.api.feature.wallet.dto.response.WalletResponse;
import com.mentorx.api.feature.wallet.dto.response.WalletTransactionResponse;
import com.mentorx.api.feature.wallet.entity.Wallet;
import com.mentorx.api.feature.wallet.entity.WalletTransaction;
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
}