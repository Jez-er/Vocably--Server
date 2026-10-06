package com.vocably.auth.exception;

import org.springframework.http.HttpStatus;

import com.vocably.common.error.ApiException;
import com.vocably.common.error.ErrorCode;

/** A token was malformed, expired, of the wrong type, already used, or revoked. */
public class InvalidTokenException extends ApiException {

    public InvalidTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN, message);
    }

    public InvalidTokenException() {
        this("Invalid or expired token");
    }
}
