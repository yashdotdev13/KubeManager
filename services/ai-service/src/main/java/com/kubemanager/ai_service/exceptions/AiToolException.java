package com.kubemanager.ai_service.exceptions;

public class AiToolException extends AiServiceException {

    public AiToolException(String message) {
        super(message);
    }

    public AiToolException(String message, Throwable cause) {
        super(message, cause);
    }
}