package com.vocably.common.error;

/**
 * Machine-readable error identifiers.
 *
 * <p>Every error body carries one of these in its {@code code} field so a client can branch on the
 * cause instead of guessing from the numeric status (several distinct failures share a status).
 */
public enum ErrorCode {

    // 400
    VALIDATION_FAILED,
    MALFORMED_REQUEST,
    INVALID_PARAMETER,

    // 401
    UNAUTHORIZED,
    INVALID_CREDENTIALS,
    INVALID_TOKEN,
    REFRESH_TOKEN_MISSING,

    // 403
    FORBIDDEN,

    // 404
    NOT_FOUND,
    ENDPOINT_NOT_FOUND,

    // 405
    METHOD_NOT_ALLOWED,

    // 409
    CONFLICT,
    EMAIL_ALREADY_USED,
    LANGUAGE_ALREADY_EXISTS,

    // 501
    NOT_IMPLEMENTED,

    // 500
    INTERNAL_ERROR
}
