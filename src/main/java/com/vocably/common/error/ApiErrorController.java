package com.vocably.common.error;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Hidden
@RestController
public class ApiErrorController implements ErrorController {

    @RequestMapping("${server.error.path:${error.path:/error}}")
    public ResponseEntity<ApiErrorResponse> handleError(HttpServletRequest request) {
        HttpStatus status = resolveStatus(request);
        ErrorCode code = codeFor(status);

        return ResponseEntity
                .status(status)
                .body(ApiErrors.of(status, code, messageFor(request, status), originalPath(request)));
    }

    private HttpStatus resolveStatus(HttpServletRequest request) {
        Object attribute = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (attribute instanceof Integer statusCode) {
            HttpStatus resolved = HttpStatus.resolve(statusCode);

            if (resolved != null) {
                return resolved;
            }
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private ErrorCode codeFor(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> ErrorCode.MALFORMED_REQUEST;
            case UNAUTHORIZED -> ErrorCode.UNAUTHORIZED;
            case FORBIDDEN -> ErrorCode.FORBIDDEN;
            case NOT_FOUND -> ErrorCode.ENDPOINT_NOT_FOUND;
            case METHOD_NOT_ALLOWED -> ErrorCode.METHOD_NOT_ALLOWED;
            case CONFLICT -> ErrorCode.CONFLICT;
            default -> status.is5xxServerError() ? ErrorCode.INTERNAL_ERROR : ErrorCode.MALFORMED_REQUEST;
        };
    }

    private String messageFor(HttpServletRequest request, HttpStatus status) {
        if (status.is5xxServerError()) {
            return "Unexpected server error";
        }

        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        return message instanceof String text && !text.isBlank() ? text : status.getReasonPhrase();
    }

    private String originalPath(HttpServletRequest request) {
        Object uri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        return uri instanceof String path ? path : request.getRequestURI();
    }
}
