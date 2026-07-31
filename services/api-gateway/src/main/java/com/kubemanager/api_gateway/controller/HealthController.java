package com.kubemanager.api_gateway.controller;


import com.kubemanager.api_gateway.model.AuthenticatedUser;
import com.kubemanager.response.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

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



    @GetMapping("/api/v1/me")
    public Mono<AuthenticatedUser> me(Authentication authentication) {
        return Mono.just((AuthenticatedUser) authentication.getPrincipal());

    }

}