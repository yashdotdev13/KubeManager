package com.kubemanager.ai_service.exceptions;

public class AiRequestException extends AiServiceException {

    public AiRequestException(String message) {
        super(AiErrorCode.INVALID_REQUEST, message);
    }
}