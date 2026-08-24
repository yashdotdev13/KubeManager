package com.kubemanager.ai_service.agent.tool.rag;

import com.kubemanager.ai_service.agent.AiTool;
import com.kubemanager.ai_service.agent.tool.ToolDefinition;
import com.kubemanager.ai_service.agent.tool.ToolRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import com.kubemanager.ai_service.rag.retrieval.RagRetrievalService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RagKnowledgeTool implements AiTool {

    private static final String TOOL_NAME = "rag_knowledge";

    private final RagRetrievalService ragRetrievalService;

    @Override
    public String getName() {

        return TOOL_NAME;
    }

    @Override
    public ToolDefinition getToolDefinition() {

        return ToolDefinition.builder()
                .name(TOOL_NAME)
                .description(
                        "Retrieves relevant Kubernetes knowledge from the "
                                + "KubeManager knowledge base using semantic search. "
                                + "Use this tool when Kubernetes documentation, "
                                + "troubleshooting knowledge, or conceptual "
                                + "information is required."
                )
                .inputSchema("""
                        {
                          "type": "object",
                          "properties": {
                            "query": {
                              "type": "string",
                              "description": "Kubernetes-related question or knowledge query"
                            }
                          },
                          "required": ["query"]
                        }
                        """)
                .build();
    }

    @Override
    public ToolResponse execute(
            ToolRequest request
    ) {

        if (request == null) {

            return failure(
                    "Tool request cannot be null."
            );
        }

        Map<String, Object> arguments =
                request.getArguments();

        if (arguments == null
                || arguments.isEmpty()) {

            return failure(
                    "Tool arguments are required."
            );
        }


        Object queryValue =
                arguments.get("query");

        if (queryValue == null
                || queryValue.toString().isBlank()) {

            return failure(
                    "query is required."
            );
        }

        String query =
                queryValue
                        .toString()
                        .trim();

        try {

            log.info(
                    "Executing '{}' with query='{}'",
                    TOOL_NAME,
                    query
            );

            List<Document> documents =
                    ragRetrievalService.retrieve(
                            query
                    );

            if (documents == null) {

                return failure(
                        "RAG retrieval returned no response."
                );
            }


            if (documents.isEmpty()) {

                log.info(
                        "No relevant Kubernetes knowledge found for query='{}'",
                        query
                );

                return ToolResponse.builder()
                        .success(true)
                        .message(
                                "No relevant Kubernetes knowledge was found."
                        )
                        .data(
                                Map.of(
                                        "query",
                                        query,
                                        "results",
                                        List.of()
                                )
                        )
                        .build();
            }


            log.info(
                    "Retrieved {} Kubernetes knowledge documents for query='{}'",
                    documents.size(),
                    query
            );

            return ToolResponse.builder()
                    .success(true)
                    .message(
                            "Relevant Kubernetes knowledge retrieved successfully."
                    )
                    .data(
                            Map.of(
                                    "query",
                                    query,
                                    "results",
                                    documents
                            )
                    )
                    .build();

        } catch (Exception exception) {

            log.error(
                    "Failed to execute '{}' for query='{}'",
                    TOOL_NAME,
                    query,
                    exception
            );

            return failure(
                    "Failed to retrieve Kubernetes knowledge."
            );
        }
    }

    private ToolResponse failure(
            String message
    ) {

        return ToolResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}