package com.vocably.common.error;

import org.springframework.http.HttpStatus;

/**
 * Base class for failures that map to a deliberate HTTP status and {@link ErrorCode}.
 *
 * <p>{@link GlobalExceptionHandler} renders any subclass straight into the standard error body, so
 * domain code throws instead of returning status codes or error strings.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode code;

    protected ApiException(HttpStatus status, ErrorCode code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ErrorCode getCode() {
        return code;
    }
}
