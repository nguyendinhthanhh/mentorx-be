package com.mentorx.api.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FptAiException extends RuntimeException {
    public FptAiException(String message) {
        super(message);
    }
}
