package com.kubemanager.ai_service.exceptions;

public class AiToolException extends AiServiceException {

    public AiToolException(String message) {
        super(AiErrorCode.TOOL_EXECUTION_FAILED, message);
    }

    public AiToolException(
            String message,
            Throwable cause
    ) {
        super(AiErrorCode.TOOL_EXECUTION_FAILED, message, cause);
    }
}