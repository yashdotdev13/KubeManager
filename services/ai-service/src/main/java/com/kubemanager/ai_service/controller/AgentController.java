package com.kubemanager.ai_service.controller;

import com.kubemanager.ai_service.agent.model.AgentRequest;
import com.kubemanager.ai_service.agent.model.AgentResponse;
import com.kubemanager.ai_service.agent.orchestrator.AgentOrchestrator;
import com.kubemanager.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AgentController {

    private final AgentOrchestrator agentOrchestrator;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AgentResponse>> chat(
            @Valid @RequestBody AgentRequest request
    ) {

        AgentResponse response =
                agentOrchestrator.process(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "AI request processed successfully.",
                        response
                )
        );
    }
}