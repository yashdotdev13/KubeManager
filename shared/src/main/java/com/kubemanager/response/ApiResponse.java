package com.kubemanager.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ApiResponse<T> {

    private final boolean success;

    private final String message;

    private final T data;

    @Builder.Default
    private final Instant timestamp = Instant.now();

    private final String traceId;

    /**
     * Success response with default message.
     */
    public static <T> ApiResponse<T> success(T data) {

        return ApiResponse.<T>builder()
                .success(true)
                .message("Request processed successfully.")
                .data(data)
                .traceId(null)
                .build();
    }

    /**
     * Success response with custom message.
     */
    public static <T> ApiResponse<T> success(
            String message,
            T data
    ) {

        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .traceId(null)
                .build();
    }

    /**
     * Success response without data.
     */
    public static ApiResponse<Void> success() {

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Request processed successfully.")
                .data(null)
                .traceId(null)
                .build();
    }

    /**
     * Success response without data but with custom message.
     */
    public static ApiResponse<Void> success(
            String message
    ) {

        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .data(null)
                .traceId(null)
                .build();
    }

}