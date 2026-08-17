package com.kubemanager.ai_service.exceptions;

public class AiPlanningException extends AiServiceException {

    public AiPlanningException(String message) {
        super(AiErrorCode.PLANNING_FAILED, message);
    }

    public AiPlanningException(
            String message,
            Throwable cause
    ) {
        super(AiErrorCode.PLANNING_FAILED, message, cause);
    }
}