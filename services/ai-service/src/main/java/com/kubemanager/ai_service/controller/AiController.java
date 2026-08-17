package com.kubemanager.ai_service.controller;

import com.kubemanager.ai_service.dto.AiChatRequest;
import com.kubemanager.ai_service.dto.AiChatResponse;
import com.kubemanager.ai_service.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(
            @Valid @RequestBody AiChatRequest request
    ) {

        return ResponseEntity.ok(
                aiService.chat(request)
        );
    }
}