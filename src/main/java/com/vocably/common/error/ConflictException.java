package com.vocably.common.error;

import org.springframework.http.HttpStatus;

/** The request conflicts with the current state of a resource. */
public class ConflictException extends ApiException {

    public ConflictException(ErrorCode code, String message) {
        super(HttpStatus.CONFLICT, code, message);
    }

    public ConflictException(String message) {
        this(ErrorCode.CONFLICT, message);
    }
}
