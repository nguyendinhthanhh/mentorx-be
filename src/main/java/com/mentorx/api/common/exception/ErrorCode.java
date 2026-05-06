package com.mentorx.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // User errors
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "User already exists"),
    USER_INACTIVE(HttpStatus.FORBIDDEN, "User account is inactive"),
    USER_SUSPENDED(HttpStatus.FORBIDDEN, "User account is suspended"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid credentials"),
    
    // Mentor errors
    MENTOR_NOT_FOUND(HttpStatus.NOT_FOUND, "Mentor not found"),
    MENTOR_NOT_APPROVED(HttpStatus.FORBIDDEN, "Mentor is not approved"),
    MENTOR_APPLICATION_EXISTS(HttpStatus.CONFLICT, "Mentor application already exists"),
    MENTOR_APPLICATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "Mentor application already exists"),
    MENTOR_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "Mentor profile not found"),
    MENTOR_PROFILE_CANNOT_BE_UPDATED(HttpStatus.FORBIDDEN, "Mentor profile cannot be updated"),
    
    // Job errors
    JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "Job not found"),
    JOB_NOT_AVAILABLE(HttpStatus.FORBIDDEN, "Job is not available"),
    UNAUTHORIZED_JOB_ACCESS(HttpStatus.FORBIDDEN, "Unauthorized access to job"),
    PROPOSAL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Proposal already exists for this job"),
    PROPOSAL_NOT_FOUND(HttpStatus.NOT_FOUND, "Proposal not found"),
    CONTRACT_NOT_FOUND(HttpStatus.NOT_FOUND, "Contract not found"),
    MILESTONE_NOT_FOUND(HttpStatus.NOT_FOUND, "Milestone not found"),
    INVALID_MILESTONE_STATUS(HttpStatus.BAD_REQUEST, "Invalid milestone status transition"),
    
    // Course errors
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "Course not found"),
    COURSE_NOT_PUBLISHED(HttpStatus.FORBIDDEN, "Course is not published"),
    COURSE_SECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Course section not found"),
    SECTION_ORDER_EXISTS(HttpStatus.BAD_REQUEST, "Section order already exists for this course"),
    LESSON_NOT_FOUND(HttpStatus.NOT_FOUND, "Lesson not found"),
    LESSON_ORDER_EXISTS(HttpStatus.BAD_REQUEST, "Lesson order already exists for this section"),
    ENROLLMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Enrollment not found"),
    ALREADY_ENROLLED(HttpStatus.CONFLICT, "Already enrolled in this course"),
    LESSON_PROGRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "Lesson progress not found"),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Comment not found"),
    COMMENT_DELETED(HttpStatus.NOT_FOUND, "Comment has been deleted"),
    COMMENT_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "You can only update your own comments"),
    COMMENT_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "You can only delete your own comments"),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "Cart item not found"),
    COURSE_ALREADY_IN_CART(HttpStatus.BAD_REQUEST, "Course is already in cart"),
    
    // Wallet errors
    WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "Wallet not found"),
    WALLET_ALREADY_EXISTS(HttpStatus.CONFLICT, "Wallet already exists"),
    WALLET_FROZEN(HttpStatus.FORBIDDEN, "Wallet is frozen"),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "Insufficient MXC balance"),
    INVALID_TRANSACTION_AMOUNT(HttpStatus.BAD_REQUEST, "Invalid transaction amount"),
    TRANSACTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Transaction failed"),
    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Transaction not found"),
    INVALID_TRANSACTION_TYPE(HttpStatus.BAD_REQUEST, "Invalid transaction type"),
    WITHDRAWAL_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "Withdrawal limit exceeded"),
    WITHDRAWAL_PENDING_EXISTS(HttpStatus.CONFLICT, "A pending withdrawal request already exists"),
    WITHDRAWAL_NOT_FOUND(HttpStatus.NOT_FOUND, "Withdrawal request not found"),
    INVALID_PAYMENT_METHOD(HttpStatus.BAD_REQUEST, "Invalid payment method"),
    DEPOSIT_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Deposit order not found"),
    DEPOSIT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "Deposit order already processed"),
    ESCROW_NOT_FOUND(HttpStatus.NOT_FOUND, "Escrow record not found"),
    ESCROW_ALREADY_RELEASED(HttpStatus.CONFLICT, "Escrow has already been released"),
    ESCROW_ALREADY_REFUNDED(HttpStatus.CONFLICT, "Escrow has already been refunded"),
    ESCROW_INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "Insufficient escrow balance for this operation"),
    
    // Chat errors
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "Chat room not found"),
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "Message not found"),
    UNAUTHORIZED_CHAT_ACCESS(HttpStatus.FORBIDDEN, "Unauthorized access to chat"),
    USER_BLOCKED(HttpStatus.FORBIDDEN, "User is blocked"),
    
    // Dispute & Report errors
    DISPUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "Dispute not found"),
    DISPUTE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Dispute already exists for this contract"),
    INVALID_DISPUTE_STATUS(HttpStatus.BAD_REQUEST, "Invalid dispute status"),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "Report not found"),
    
    // Review errors
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "Review not found"),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "Review already exists"),
    CANNOT_REVIEW_SELF(HttpStatus.BAD_REQUEST, "Cannot review yourself"),
    REVIEW_CANNOT_BE_EDITED(HttpStatus.FORBIDDEN, "Review cannot be edited at this time"),
    REVIEW_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "Review report not found"),
    
    // Notification errors
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Notification not found"),
    
    // Authentication errors
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token has expired"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid token"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "Refresh token not found"),
    
    // File errors
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "File not found"),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed"),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "Invalid file type"),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "File size exceeded"),
    
    // General errors
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation error"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad request");

    private final HttpStatus status;
    private final String message;
}