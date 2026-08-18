package com.kubemanager.ai_service.agent.model;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgentRequest {

    @NotBlank(message = "message is required")
    private String message;
}
