package com.kubemanager.ai_service.orchestrator;

import com.kubemanager.ai_service.agent.context.AgentContextService;
import com.kubemanager.ai_service.agent.memory.AgentMemory;
import com.kubemanager.ai_service.agent.memory.AgentMemoryService;
import com.kubemanager.ai_service.agent.memory.MemoryExtractionService;
import com.kubemanager.ai_service.agent.memory.MemoryRetrievalService;
import com.kubemanager.ai_service.agent.model.AgentRequest;
import com.kubemanager.ai_service.agent.orchestrator.AgentOrchestratorServiceImpl;
import com.kubemanager.ai_service.agent.workflow.AgentWorkflowExecutor;
import com.kubemanager.ai_service.agent.workflow.AgentWorkflowResult;
import com.kubemanager.ai_service.auth.UserContext;
import com.kubemanager.ai_service.auth.UserContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentOrchestratorServiceImplTest {

    @Mock
    private AgentWorkflowExecutor agentWorkflowExecutor;

    @Mock
    private AgentContextService agentContextService;

    @Mock
    private MemoryExtractionService memoryExtractionService;

    @Mock
    private AgentMemoryService agentMemoryService;

    @Mock
    private MemoryRetrievalService memoryRetrievalService;

    @InjectMocks
    private AgentOrchestratorServiceImpl orchestrator;

    @AfterEach
    void clearUserContext() {
        UserContextHolder.clear();
    }

    @Test
    void shouldPassRetrievedMemoriesToWorkflowExecutor() {
        UUID userId = UUID.fromString(
                "00000000-0000-0000-0000-000000000001"
        );

        String userIdString = userId.toString();

        String userMessage =
                "Help me troubleshoot my Kubernetes cluster.";

        UserContext userContext = mock(UserContext.class);

        when(userContext.getUserId()).thenReturn(userId);

        UserContextHolder.set(userContext);

        AgentMemory firstMemory = mock(AgentMemory.class);

        when(firstMemory.getContent()).thenReturn(
                "The user's cluster runs in the staging environment."
        );

        AgentMemory secondMemory = mock(AgentMemory.class);

        when(secondMemory.getContent()).thenReturn(
                "The user prefers step-by-step troubleshooting."
        );

        when(agentContextService.getContext(userIdString))
                .thenReturn(null);

        when(memoryRetrievalService.retrieveRelevantMemories(
                userIdString,
                userMessage
        )).thenReturn(List.of(firstMemory, secondMemory));

        AgentWorkflowResult workflowResult =
                mock(AgentWorkflowResult.class);

        when(workflowResult.getFinalResponse()).thenReturn(
                "Here are the troubleshooting steps."
        );

        when(workflowResult.getFinalToolName()).thenReturn(null);
        when(workflowResult.getFinalToolResult()).thenReturn(null);
        when(workflowResult.getExecutionSteps()).thenReturn(List.of());
        when(agentWorkflowExecutor.execute(
                eq(userMessage),
                anyList()
        )).thenReturn(workflowResult);

        when(memoryExtractionService.extract(
                anyString(),
                any(),
                any()
        )).thenReturn(null);

        AgentRequest request = mock(AgentRequest.class);

        when(request.getMessage()).thenReturn(userMessage);

        var response = orchestrator.process(request);

        assertNotNull(response);

        assertEquals(
                "Here are the troubleshooting steps.",
                response.getMessage()
        );

        ArgumentCaptor<List<String>> memoriesCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(agentWorkflowExecutor).execute(
                eq(userMessage),
                memoriesCaptor.capture()
        );

        assertEquals(
                List.of(
                        "The user's cluster runs in the staging environment.",
                        "The user prefers step-by-step troubleshooting."
                ),
                memoriesCaptor.getValue()
        );

        verify(memoryRetrievalService).retrieveRelevantMemories(
                userIdString,
                userMessage
        );
    }

    @Test
    void shouldContinueWithoutMemoriesWhenRetrievalFails() {
        UUID userId = UUID.fromString(
                "00000000-0000-0000-0000-000000000002"
        );

        String userIdString = userId.toString();

        String userMessage = "Check my cluster health.";

        UserContext userContext = mock(UserContext.class);

        when(userContext.getUserId()).thenReturn(userId);

        UserContextHolder.set(userContext);

        when(agentContextService.getContext(userIdString))
                .thenReturn(null);

        when(memoryRetrievalService.retrieveRelevantMemories(
                userIdString,
                userMessage
        )).thenThrow(
                new RuntimeException("Memory store unavailable")
        );

        AgentWorkflowResult workflowResult =
                mock(AgentWorkflowResult.class);

        when(workflowResult.getFinalResponse()).thenReturn(
                "I can help check your cluster."
        );

        when(workflowResult.getFinalToolName()).thenReturn(null);
        when(workflowResult.getFinalToolResult()).thenReturn(null);
        when(workflowResult.getExecutionSteps()).thenReturn(List.of());

        when(agentWorkflowExecutor.execute(
                eq(userMessage),
                anyList()
        )).thenReturn(workflowResult);

        when(memoryExtractionService.extract(
                anyString(),
                any(),
                any()
        )).thenReturn(null);

        AgentRequest request = mock(AgentRequest.class);

        when(request.getMessage()).thenReturn(userMessage);

        var response = orchestrator.process(request);

        assertNotNull(response);

        assertEquals(
                "I can help check your cluster.",
                response.getMessage()
        );

        verify(agentWorkflowExecutor).execute(
                eq(userMessage),
                eq(List.of())
        );
    }
}
