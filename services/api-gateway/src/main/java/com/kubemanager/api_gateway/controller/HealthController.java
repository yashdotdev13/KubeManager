package com.kubemanager.api_gateway.controller;


import com.kubemanager.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class HealthController {

    @GetMapping("/api/v1/health")
    public ApiResponse<Map<String, String>> health() {

        return ApiResponse.<Map<String, String>>builder()
                .success(true)
                .message("API Gateway is running")
                .data(Map.of("status", "UP"))
                .traceId(UUID.randomUUID().toString())
                .build();
    }

}