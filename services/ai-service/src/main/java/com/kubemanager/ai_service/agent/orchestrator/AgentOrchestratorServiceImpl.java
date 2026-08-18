package com.kubemanager.ai_service.agent.orchestrator;

import com.kubemanager.ai_service.agent.chat.AiChatService;
import com.kubemanager.ai_service.agent.model.AgentPlan;
import com.kubemanager.ai_service.agent.model.AgentRequest;
import com.kubemanager.ai_service.agent.model.AgentResponse;
import com.kubemanager.ai_service.agent.planner.AgentPlanner;
import com.kubemanager.ai_service.agent.tool.ToolExecutor;
import com.kubemanager.ai_service.agent.tool.ToolRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AgentOrchestratorServiceImpl implements AgentOrchestrator{


    private final AiChatService aiChatService;
    private final AgentPlanner agentPlanner;
    private final ToolExecutor toolExecutor;


    @Override
    public AgentResponse process(AgentRequest request) {


        AgentPlan plan =
                agentPlanner.createPlan(request);

        if (!plan.isRequiresTool()) {

            String response =
                    aiChatService.chat(request.getMessage());

            return AgentResponse.builder()
                    .message(response)
                    .build();
        }

        ToolRequest toolRequest =
                ToolRequest.builder()
                        .toolName(plan.getToolName())
                        .arguments(plan.getArguments())
                        .build();

        ToolResponse toolResponse =
                toolExecutor.execute(toolRequest);

        String finalPrompt = """
                You are KubeManager AI Agent.

                User request:
                %s

                Tool executed:
                %s

                Tool result:
                %s

                Provide a clear and useful answer to the user.

                Do not mention internal implementation details
                unless necessary.
                """.formatted(
                request.getMessage(),
                plan.getToolName(),
                toolResponse
        );

        String finalResponse =
                aiChatService.chat(finalPrompt);

        return AgentResponse.builder()
                .message(finalResponse)
                .build();
    }
}
