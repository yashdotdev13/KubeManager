package com.kubemanager.response;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class ApiResponse<T> {


    private final boolean success;
    private final String message;
    private final T data;

    @Builder.Default
    private final Instant timestamp = Instant.now();
    private final String traceId;
}
