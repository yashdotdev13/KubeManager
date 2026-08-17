package com.kubemanager.ai_service.exceptions;

public class AiContextException extends AiServiceException {

    public AiContextException(String message) {
        super(message);
    }

    public AiContextException(String message, Throwable cause) {
        super(message, cause);
    }
}