package com.kubemanager.ai_service.exceptions;

public enum AiErrorCode {

    INVALID_REQUEST,
    MODEL_ERROR,
    MODEL_TIMEOUT,
    MODEL_RATE_LIMITED,
    TOOL_EXECUTION_FAILED,
    TOOL_UNAVAILABLE,
    PLANNING_FAILED,
    EXECUTION_FAILED,
    CONTEXT_ERROR,
    INTERNAL_ERROR
}