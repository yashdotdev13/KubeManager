package com.kubemanager.response;


import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class ErrorResponse {

    private final String errorCode;
    private final String message;
    private final List<String> errors;

    @Builder.Default
    private final Instant timestamp = Instant.now();
    private final String traceId;

}
