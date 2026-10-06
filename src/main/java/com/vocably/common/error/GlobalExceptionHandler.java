package com.vocably.common.error;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

/**
 * Turns every exception that escapes a controller into the standard {@link ApiErrorResponse}.
 *
 * <p>Without this, Spring Boot answers an uncaught {@code RuntimeException} with a bare 500 and its
 * own error body, which is both the wrong status for a domain failure and a second, incompatible
 * payload shape for clients to parse.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Domain failures carry their own status and code. */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException exception, HttpServletRequest request) {
        return respond(exception.getStatus(), exception.getCode(), exception.getMessage(), request, null);
    }

    /** Bean validation on a {@code @Valid @RequestBody}, reported per field. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            // Keep the first message per field: a field with several violations would otherwise
            // report whichever constraint happened to run last.
            fieldErrors.putIfAbsent(fieldError.getField(), messageOf(fieldError));
        }

        exception.getBindingResult().getGlobalErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getObjectName(), error.getDefaultMessage())
        );

        return respond(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_FAILED,
                "Request validation failed",
                request,
                fieldErrors
        );
    }

    /** Bean validation on path variables, request params or a validated service call. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            fieldErrors.putIfAbsent(lastPathNode(violation), violation.getMessage());
        }

        return respond(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_FAILED,
                "Request validation failed",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return respond(
                HttpStatus.BAD_REQUEST,
                ErrorCode.MALFORMED_REQUEST,
                "Request body is missing or malformed",
                request,
                null
        );
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiErrorResponse> handleBadParameter(Exception exception, HttpServletRequest request) {
        String name = exception instanceof MethodArgumentTypeMismatchException mismatch
                ? mismatch.getName()
                : ((MissingServletRequestParameterException) exception).getParameterName();

        return respond(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_PARAMETER,
                "Invalid or missing request parameter: " + name,
                request,
                Map.of(name, "Invalid or missing value")
        );
    }

    /**
     * A missing refresh-token cookie. 401 rather than 400: to a client this is indistinguishable
     * from an expired session, and treating it as a bad request would make "logged out" look like a
     * programming error.
     */
    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingCookie(
            MissingRequestCookieException exception,
            HttpServletRequest request
    ) {
        return respond(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.REFRESH_TOKEN_MISSING,
                "Missing cookie: " + exception.getCookieName(),
                request,
                null
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return respond(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, "Access is denied", request, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(
            AuthenticationException exception,
            HttpServletRequest request
    ) {
        return respond(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.UNAUTHORIZED,
                "Authentication is required to access this resource",
                request,
                null
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResource(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        return respond(HttpStatus.NOT_FOUND, ErrorCode.ENDPOINT_NOT_FOUND, "No endpoint for this path", request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        return respond(
                HttpStatus.METHOD_NOT_ALLOWED,
                ErrorCode.METHOD_NOT_ALLOWED,
                exception.getMethod() + " is not supported for this endpoint",
                request,
                null
        );
    }

    /** A unique or foreign-key constraint the application did not check up front. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        log.warn("Data integrity violation on {}", ApiErrors.pathOf(request), exception);

        return respond(
                HttpStatus.CONFLICT,
                ErrorCode.CONFLICT,
                "The request conflicts with existing data",
                request,
                null
        );
    }

    /**
     * Anything unanticipated.
     *
     * <p>The message is deliberately generic: an exception message can carry SQL, class names or
     * user data, none of which belongs in a client response. The stack trace goes to the log.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception on {}", ApiErrors.pathOf(request), exception);

        return respond(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR,
                "Unexpected server error",
                request,
                null
        );
    }

    private ResponseEntity<ApiErrorResponse> respond(
            HttpStatus status,
            ErrorCode code,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors
    ) {
        return ResponseEntity
                .status(status)
                .body(ApiErrors.of(status, code, message, ApiErrors.pathOf(request), fieldErrors));
    }

    private String messageOf(FieldError fieldError) {
        return fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value";
    }

    private String lastPathNode(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        int separator = path.lastIndexOf('.');

        return separator < 0 ? path : path.substring(separator + 1);
    }
}
