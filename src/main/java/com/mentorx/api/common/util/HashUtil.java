package com.mentorx.api.common.util;

import com.mentorx.api.common.enums.LedgerDirection;
import com.mentorx.api.common.enums.TxnType;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Slf4j
public final class HashUtil {

    private HashUtil() {
        // Utility class
    }

    public static String generateSHA256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("Failed to generate hash", e);
        }
    }

    public static String generateWalletEntryHash(Long walletId, String txnType, String direction, 
                                               Long amount, Long balanceAfter, String prevHash, String secretKey) {
        String input = walletId + ":" + txnType + ":" + direction + ":" + amount + ":" + 
                      balanceAfter + ":" + prevHash + ":" + secretKey;
        return generateSHA256Hash(input);
    }

    public static String generateTransactionHash(UUID walletId, TxnType txnType, LedgerDirection direction,
                                               BigDecimal amount, BigDecimal balanceAfter, String prevHash, String secretKey) {
        String input = walletId + ":" + txnType + ":" + direction + ":" + amount + ":" + 
                      balanceAfter + ":" + (prevHash != null ? prevHash : "") + ":" + secretKey;
        return generateSHA256Hash(input);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}