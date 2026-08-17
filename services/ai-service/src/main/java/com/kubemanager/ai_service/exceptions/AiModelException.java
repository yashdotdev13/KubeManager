package com.kubemanager.ai_service.exceptions;

public class AiModelException extends AiServiceException {

    public AiModelException(String message) {
        super(message);
    }

    public AiModelException(String message, Throwable cause) {
        super(message, cause);
    }
}