package com.kubemanager.ai_service.agent.model;


import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AgentRequest {

    @NotBlank(message = "message is required")
    private String message;
}
