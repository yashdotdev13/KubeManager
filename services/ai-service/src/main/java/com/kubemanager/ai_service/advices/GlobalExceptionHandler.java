package com.kubemanager.ai_service.advices;


import com.kubemanager.ai_service.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AiRequestException.class)
    public ResponseEntity<ErrorResponse> handleAiRequestException(
            AiRequestException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getErrorCode(),
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(AiModelException.class)
    public ResponseEntity<ErrorResponse> handleAiModelException(
            AiModelException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_GATEWAY,
                exception.getErrorCode(),
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(AiToolException.class)
    public ResponseEntity<ErrorResponse> handleAiToolException(
            AiToolException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_GATEWAY,
                exception.getErrorCode(),
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(AiPlanningException.class)
    public ResponseEntity<ErrorResponse> handleAiPlanningException(
            AiPlanningException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getErrorCode(),
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(AiExecutionException.class)
    public ResponseEntity<ErrorResponse> handleAiExecutionException(
            AiExecutionException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getErrorCode(),
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(AiContextException.class)
    public ResponseEntity<ErrorResponse> handleAiContextException(
            AiContextException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getErrorCode(),
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<ErrorResponse> handleAiServiceException(
            AiServiceException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getErrorCode(),
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception exception,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                AiErrorCode.INTERNAL_ERROR,
                "An unexpected error occurred",
                request
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            AiErrorCode errorCode,
            String message,
            HttpServletRequest request
    ) {

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                errorCode,
                message,
                request.getRequestURI()
        );
        return ResponseEntity
                .status(status)
                .body(response);
    }
}