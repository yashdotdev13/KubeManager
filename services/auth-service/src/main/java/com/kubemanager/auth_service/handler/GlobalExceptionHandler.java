package com.kubemanager.auth_service.handler;


import com.kubemanager.auth_service.exception.AuthenticationException;
import com.kubemanager.auth_service.exception.InvalidTokenException;
import com.kubemanager.auth_service.exception.TokenExpiredException;
import com.kubemanager.exception.*;
import com.kubemanager.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                exception.getErrorCode().name(),
                exception.getMessage(),
                List.of(),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(
            BadRequestException exception
    ) {

        return buildErrorResponse(
                exception.getErrorCode().name(),
                exception.getMessage(),
                List.of(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(
            ConflictException exception
    ) {

        return buildErrorResponse(
                exception.getErrorCode().name(),
                exception.getMessage(),
                List.of(),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException exception
    ) {

        return buildErrorResponse(
                exception.getErrorCode().name(),
                exception.getMessage(),
                List.of(),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(
            ForbiddenException exception
    ) {

        return buildErrorResponse(
                exception.getErrorCode().name(),
                exception.getMessage(),
                List.of(),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception
    ) {

        List<String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        return buildErrorResponse(
                "VALIDATION_FAILED",
                "Validation failed.",
                errors,
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception exception
    ) {

        return buildErrorResponse(
                "INTERNAL_SERVER_ERROR",
                exception.getMessage(),
                List.of(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }


    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException exception
    ) {

        return buildErrorResponse(
                exception.getErrorCode().name(),
                exception.getMessage(),
                List.of(),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(
            InvalidTokenException exception
    ) {

        return buildErrorResponse(
                exception.getErrorCode().name(),
                exception.getMessage(),
                List.of(),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpiredException(
            TokenExpiredException exception
    ) {

        return buildErrorResponse(
                exception.getErrorCode().name(),
                exception.getMessage(),
                List.of(),
                HttpStatus.UNAUTHORIZED
        );
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            String errorCode,
            String message,
            List<String> errors,
            HttpStatus status
    ) {

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .errors(errors)
                .traceId(MDC.get("traceId"))
                .build();

        return ResponseEntity.status(status).body(response);
    }

}