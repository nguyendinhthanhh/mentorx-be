package com.mentorx.api.common.exception;

import com.mentorx.api.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex) {
        HttpStatus status = ex.getErrorCode().getStatus();
        if (status.is4xxClientError()) {
            // Expected business/auth failures (e.g. invalid credentials) should not pollute logs with stacktraces.
            log.warn("Application exception [{}]: {}", status.value(), ex.getMessage());
        } else {
            log.error("Application exception [{}]: {}", status.value(), ex.getMessage(), ex);
        }
        return ResponseEntity
            .status(status)
            .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("Validation exception: {}", errors);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Validation failed", errors));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error("Access denied"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("Bad credentials");
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("Email hoặc mật khẩu không đúng. Vui lòng thử lại."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalStateException(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(FptAiException.class)
    public ResponseEntity<ApiResponse<Object>> handleFptAiException(FptAiException ex) {
        log.error("FPT AI service error: {}", ex.getMessage());
        
        // Extract user-friendly message from FPT AI error
        String userMessage = extractFptAiUserMessage(ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(userMessage));
    }

    @ExceptionHandler(KycRejectedException.class)
    public ResponseEntity<ApiResponse<Object>> handleKycRejectedException(KycRejectedException ex) {
        log.warn("KYC rejected: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(FrameExtractionException.class)
    public ResponseEntity<ApiResponse<Object>> handleFrameExtractionException(FrameExtractionException ex) {
        log.error("Frame extraction failed: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Không thể xử lý video. Vui lòng thử lại với video khác."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("An unexpected error occurred"));
    }

    /**
     * Extract user-friendly message from FPT AI error response
     */
    private String extractFptAiUserMessage(String errorMessage) {
        if (errorMessage == null) {
            return "Lỗi xác thực danh tính. Vui lòng thử lại.";
        }

        // Check for common FPT AI error messages
        if (errorMessage.contains("Unable to find ID card")) {
            return "Không tìm thấy CCCD trong ảnh. Vui lòng chụp lại ảnh CCCD rõ nét, đầy đủ 4 góc.";
        }
        if (errorMessage.contains("Image quality")) {
            return "Chất lượng ảnh không đủ tốt. Vui lòng chụp ảnh rõ nét hơn.";
        }
        if (errorMessage.contains("Face not found")) {
            return "Không tìm thấy khuôn mặt trong ảnh. Vui lòng chụp lại.";
        }
        if (errorMessage.contains("Liveness check failed")) {
            return "Xác thực khuôn mặt thất bại. Vui lòng quay video với khuôn mặt thật.";
        }
        if (errorMessage.contains("Face not match")) {
            return "Khuôn mặt không khớp với ảnh trên CCCD. Vui lòng thử lại.";
        }

        // Default message for other FPT AI errors
        return "Lỗi xác thực danh tính: " + errorMessage + ". Vui lòng kiểm tra lại ảnh và thử lại.";
    }
}
