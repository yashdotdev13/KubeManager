package com.kubemanager.api_gateway.handler;


import com.kubemanager.exception.BaseException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException exception) {

        ErrorCode errorCode = exception.getErrorCode();

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(errorCode.name())
                .message(exception.getMessage())
                .errors(List.of())
                .traceId(UUID.randomUUID().toString())
                .build();

        return ResponseEntity
                .status(resolveStatus(errorCode))
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.name())
                .message(exception.getMessage())
                .errors(List.of())
                .traceId(UUID.randomUUID().toString())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    private HttpStatus resolveStatus(ErrorCode errorCode) {

        return switch (errorCode) {

            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case VALIDATION_ERROR -> HttpStatus.UNPROCESSABLE_ENTITY;

            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

}