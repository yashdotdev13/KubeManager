package com.kubemanager.ai_service.exceptions;

public class AiModelException extends AiServiceException {

    public AiModelException(String message) {
        super(AiErrorCode.MODEL_ERROR, message);
    }

    public AiModelException(
            String message,
            Throwable cause
    ) {
        super(AiErrorCode.MODEL_ERROR, message, cause);
    }
}