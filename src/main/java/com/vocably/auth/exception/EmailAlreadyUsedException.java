package com.vocably.auth.exception;

import com.vocably.common.error.ConflictException;
import com.vocably.common.error.ErrorCode;

/** Registration was attempted with an email that already has an account. */
public class EmailAlreadyUsedException extends ConflictException {

    public EmailAlreadyUsedException() {
        super(ErrorCode.EMAIL_ALREADY_USED, "Email is already registered");
    }
}
