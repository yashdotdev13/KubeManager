package com.kubemanager.ai_service.asvices;

import com.kubemanager.ai_service.exceptions.AiErrorCode;
import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        AiErrorCode code,
        String message,
        String path
) {
}