package com.kubemanager.ai_service.exceptions;

public class AiExecutionException extends AiServiceException {

    public AiExecutionException(String message) {
        super(message);
    }

    public AiExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}