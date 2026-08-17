package com.kubemanager.ai_service.exceptions;

public class AiExecutionException extends AiServiceException {

    public AiExecutionException(String message) {
        super(AiErrorCode.EXECUTION_FAILED, message);
    }

    public AiExecutionException(
            String message,
            Throwable cause
    ) {
        super(AiErrorCode.EXECUTION_FAILED, message, cause);
    }
}