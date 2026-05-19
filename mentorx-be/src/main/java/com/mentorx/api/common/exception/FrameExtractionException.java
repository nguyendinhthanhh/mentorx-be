package com.mentorx.api.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FrameExtractionException extends RuntimeException {
    public FrameExtractionException(String message) {
        super(message);
    }

    public FrameExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
