package com.vocably.common.error;

import org.springframework.http.HttpStatus;

/** The endpoint exists and its contract is fixed, but the feature behind it is not wired up yet. */
public class NotImplementedException extends ApiException {

    public NotImplementedException(String message) {
        super(HttpStatus.NOT_IMPLEMENTED, ErrorCode.NOT_IMPLEMENTED, message);
    }
}
