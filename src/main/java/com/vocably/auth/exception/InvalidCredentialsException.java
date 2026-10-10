package com.vocably.auth.exception;

import org.springframework.http.HttpStatus;

import com.vocably.common.error.ApiException;
import com.vocably.common.error.ErrorCode;

public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_CREDENTIALS, "Invalid email or password");
    }
}
