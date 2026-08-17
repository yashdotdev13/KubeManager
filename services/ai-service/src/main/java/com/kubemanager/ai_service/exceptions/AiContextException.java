package com.kubemanager.ai_service.exceptions;

public class AiContextException extends AiServiceException {

    public AiContextException(String message) {
        super(AiErrorCode.CONTEXT_ERROR, message);
    }

    public AiContextException(
            String message,
            Throwable cause
    ) {
        super(AiErrorCode.CONTEXT_ERROR, message, cause);
    }
}