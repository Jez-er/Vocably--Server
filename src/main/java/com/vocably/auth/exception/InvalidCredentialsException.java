package com.vocably.auth.exception;

import org.springframework.http.HttpStatus;

import com.vocably.common.error.ApiException;
import com.vocably.common.error.ErrorCode;

/**
 * The supplied email/password pair did not authenticate.
 *
 * <p>The message never says which half was wrong: distinguishing them would let an attacker probe
 * for registered addresses.
 */
public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_CREDENTIALS, "Invalid email or password");
    }
}
