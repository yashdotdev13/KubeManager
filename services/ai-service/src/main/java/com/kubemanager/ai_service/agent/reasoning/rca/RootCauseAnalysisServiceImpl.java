package com.kubemanager.ai_service.agent.reasoning.rca;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kubemanager.ai_service.agent.workflow.AgentWorkflowContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RootCauseAnalysisServiceImpl
        implements RootCauseAnalysisService {

    private final ChatClient chatClient;

    private final ObjectMapper objectMapper;

    @Override
    public RootCauseAnalysis analyze(
            String userRequest,
            AgentWorkflowContext workflowContext
    ) {

        if (userRequest == null
                || userRequest.isBlank()) {

            throw new IllegalArgumentException(
                    "User request cannot be null or blank."
            );
        }

        if (workflowContext == null) {

            throw new IllegalArgumentException(
                    "Workflow context cannot be null."
            );
        }

        String evidence =
                buildEvidence(
                        workflowContext
                );

        String prompt = """
                You are the Root Cause Analysis engine of
                KubeManager AI.

                Your job is to analyze REAL Kubernetes evidence
                collected during the current agent workflow.

                Do not invent Kubernetes information.

                Do not assume evidence that was not provided.

                Base your diagnosis only on the supplied evidence.

                --------------------------------------------------
                USER REQUEST
                --------------------------------------------------

                %s

                --------------------------------------------------
                KUBERNETES EVIDENCE
                --------------------------------------------------

                %s

                --------------------------------------------------
                ANALYSIS REQUIREMENTS
                --------------------------------------------------

                Analyze the available evidence and determine:

                1. What is happening?
                2. What is the most likely root cause?
                3. What evidence supports the diagnosis?
                4. How confident are you?
                5. What should be done next?

                If the evidence is insufficient to determine
                a root cause, explicitly say that more information
                is required.

                Do not manufacture a root cause.

                --------------------------------------------------
                RESPONSE FORMAT
                --------------------------------------------------

                Return ONLY valid JSON:

                {
                  "summary": "short description of the problem",
                  "rootCause": "most likely root cause",
                  "evidence": [
                    "evidence supporting the diagnosis"
                  ],
                  "confidence": "HIGH | MEDIUM | LOW",
                  "recommendations": [
                    "recommended next action"
                  ]
                }

                Do not return markdown.
                Do not wrap the JSON in code blocks.
                Do not add text outside the JSON.
                """.formatted(
                userRequest,
                evidence
        );

        try {

            String rawResponse =
                    chatClient
                            .prompt()
                            .user(prompt)
                            .call()
                            .content();

            if (rawResponse == null
                    || rawResponse.isBlank()) {

                throw new IllegalStateException(
                        "RCA engine returned an empty response."
                );
            }

            String jsonResponse =
                    cleanJsonResponse(
                            rawResponse
                    );

            RootCauseAnalysis analysis =
                    objectMapper.readValue(
                            jsonResponse,
                            RootCauseAnalysis.class
                    );

            validateAnalysis(
                    analysis
            );

            return analysis;

        } catch (Exception exception) {

            log.error(
                    "Failed to perform root cause analysis.",
                    exception
            );

            throw new IllegalStateException(
                    "Failed to perform root cause analysis.",
                    exception
            );
        }
    }

    private String buildEvidence(
            AgentWorkflowContext workflowContext
    ) {

        if (workflowContext
                .getAccumulatedContext()
                .isEmpty()) {

            return """
                    No Kubernetes evidence has been collected.
                    """;
        }

        return workflowContext
                .getAccumulatedContext()
                .entrySet()
                .stream()
                .map(entry -> """
                        TOOL:
                        %s

                        RESULT:
                        %s
                        """.formatted(
                        entry.getKey(),
                        entry.getValue()
                ))
                .reduce(
                        "",
                        (left, right) ->
                                left + "\n" + right
                );
    }

    private void validateAnalysis(
            RootCauseAnalysis analysis
    ) {

        if (analysis == null) {

            throw new IllegalStateException(
                    "RCA result cannot be null."
            );
        }

        if (analysis.getSummary() == null
                || analysis.getSummary().isBlank()) {

            throw new IllegalStateException(
                    "RCA summary cannot be blank."
            );
        }

        if (analysis.getRootCause() == null
                || analysis.getRootCause().isBlank()) {

            throw new IllegalStateException(
                    "RCA root cause cannot be blank."
            );
        }

        if (analysis.getConfidence() == null
                || analysis.getConfidence().isBlank()) {

            throw new IllegalStateException(
                    "RCA confidence cannot be blank."
            );
        }
    }

    private String cleanJsonResponse(
            String response
    ) {

        String cleaned =
                response.trim();

        if (cleaned.startsWith("```json")) {

            cleaned =
                    cleaned
                            .substring(7)
                            .trim();

        } else if (cleaned.startsWith("```")) {

            cleaned =
                    cleaned
                            .substring(3)
                            .trim();
        }

        if (cleaned.endsWith("```")) {

            cleaned =
                    cleaned
                            .substring(
                                    0,
                                    cleaned.length() - 3
                            )
                            .trim();
        }

        return cleaned;
    }
}