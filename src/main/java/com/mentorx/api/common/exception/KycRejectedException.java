package com.mentorx.api.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class KycRejectedException extends RuntimeException {
    public KycRejectedException(String message) {
        super(message);
    }
}
