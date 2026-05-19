package com.mentorx.api.feature.payment.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class VNPayUtil {

    public static String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            log.error("Error generating HMAC SHA512", ex);
            return "";
        }
    }

    public static String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(fieldName);
                sb.append("=");
                sb.append(fieldValue);
            }
        }
        return sb.toString();
    }

    public static String hashAllFieldsEncoded(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(fieldName);
                sb.append("=");
                sb.append(java.net.URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
            }
        }
        return sb.toString();
    }

    public static String getIpAddress(HttpServletRequest request) {
        try {
            String forwarded = request.getHeader("X-FORWARDED-FOR");
            String ipAddress = (forwarded != null && !forwarded.isBlank())
                    ? forwarded.split(",")[0].trim()
                    : request.getRemoteAddr();

            if ("::1".equals(ipAddress) || "0:0:0:0:0:0:0:1".equals(ipAddress) || "localhost".equalsIgnoreCase(ipAddress)) {
                return "127.0.0.1";
            }

            if (ipAddress != null && ipAddress.startsWith("::ffff:")) {
                ipAddress = ipAddress.substring("::ffff:".length());
            }

            // VNPay works best with IPv4 input
            if (ipAddress != null && ipAddress.matches("^\\d{1,3}(\\.\\d{1,3}){3}$")) {
                return ipAddress;
            }

            return "127.0.0.1";
        } catch (Exception e) {
            log.warn("Could not resolve client IP, fallback to loopback IPv4", e);
            return "127.0.0.1";
        }
    }

    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
