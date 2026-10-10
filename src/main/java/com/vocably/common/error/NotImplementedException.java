package com.vocably.common.error;

import org.springframework.http.HttpStatus;

public class NotImplementedException extends ApiException {

    public NotImplementedException(String message) {
        super(HttpStatus.NOT_IMPLEMENTED, ErrorCode.NOT_IMPLEMENTED, message);
    }
}
