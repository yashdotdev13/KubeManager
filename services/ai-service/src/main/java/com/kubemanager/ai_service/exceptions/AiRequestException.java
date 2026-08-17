package com.kubemanager.ai_service.exceptions;

public class AiRequestException extends RuntimeException {
    public AiRequestException(String message) {
        super(message);
    }
}
