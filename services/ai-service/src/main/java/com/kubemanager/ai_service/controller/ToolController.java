package com.kubemanager.ai_service.controller;


import com.kubemanager.ai_service.agent.tool.ToolExecutor;
import com.kubemanager.ai_service.agent.tool.ToolRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import com.kubemanager.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai/tools")
@RequiredArgsConstructor
public class ToolController {

    private final ToolExecutor toolExecutor;

    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<ToolResponse>> executeTool(
            @Valid @RequestBody ToolRequest request
    ) {

        ToolResponse response =
                toolExecutor.execute(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "AI tool executed successfully.",
                        response
                )
        );
    }
}