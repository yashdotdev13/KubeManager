package com.kubemanager.ai_service.rag;

import com.kubemanager.ai_service.agent.workflow.AgentWorkflowExecutor;
import com.kubemanager.ai_service.agent.workflow.AgentWorkflowResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AgentWorkflowIntegrationTest {

    @Autowired
    private AgentWorkflowExecutor agentWorkflowExecutor;

    @Test
    void shouldAnswerKubernetesKnowledgeQuestionUsingAgentWorkflow() {

        String userMessage =
                "My Kubernetes pod is in CrashLoopBackOff. Help me troubleshoot it.";

        AgentWorkflowResult result =
                agentWorkflowExecutor.execute(
                        userMessage
                );

        assertNotNull(result);

        assertNotNull(
                result.getFinalResponse()
        );

        assertFalse(
                result.getFinalResponse().isBlank()
        );

        System.out.println(
                "\n========== AGENT WORKFLOW =========="
        );

        System.out.println(
                "Final response:"
        );

        System.out.println(
                result.getFinalResponse()
        );

        System.out.println(
                "\nExecution count: "
                        + result.getExecutionCount()
        );

        System.out.println(
                "Successful: "
                        + result.isSuccessful()
        );

        System.out.println(
                "Final tool: "
                        + result.getFinalToolName()
        );

        System.out.println(
                "Final tool result:"
        );

        System.out.println(
                result.getFinalToolResult()
        );

        System.out.println(
                "===================================="
        );
    }
}