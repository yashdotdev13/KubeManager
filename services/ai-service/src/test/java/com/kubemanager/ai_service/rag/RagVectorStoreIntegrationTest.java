package com.kubemanager.ai_service.rag;


import org.springframework.core.env.Environment;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class RagVectorStoreIntegrationTest {

    @Autowired
    private VectorStore vectorStore;

    @Test
    void shouldStoreAndRetrieveKubernetesKnowledge() {

        Document document = new Document(
                """
                Kubernetes CrashLoopBackOff occurs when a container
                repeatedly starts and then terminates unsuccessfully.
                Common causes include application crashes, incorrect
                configuration, missing environment variables, failed
                health checks, and unavailable dependencies.
                """,
                Map.of(
                        "source", "kubernetes-troubleshooting",
                        "type", "test"
                )
        );
        vectorStore.add(
                List.of(document)
        );
        List<Document> results =
                vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query(
                                        "Why does my Kubernetes pod keep restarting?"
                                )
                                .topK(3)
                                .similarityThreshold(0.0)
                                .build()
                );

        assertNotNull(results);
        assertFalse(results.isEmpty());

        System.out.println(
                "========== RAG SEARCH RESULTS =========="
        );

        results.forEach(result ->
                System.out.println(
                        result.getText()
                )
        );

        System.out.println(
                "========================================="
        );
    }


    @Test
    void shouldHaveGeminiApiKey() {

        String apiKey = System.getenv("GEMINI_API_KEY");

        assertNotNull(
                apiKey,
                "GEMINI_API_KEY is not available"
        );

        assertFalse(
                apiKey.isBlank(),
                "GEMINI_API_KEY is blank"
        );

        System.out.println(
                "GEMINI_API_KEY detected. Length = "
                        + apiKey.length()
        );
    }


    @Autowired
    private Environment environment;

    @Test
    void shouldLoadGeminiApiKey() {

        String apiKey = environment.getProperty(
                "spring.ai.google.genai.embedding.api-key"
        );

        assertNotNull(apiKey);

        assertFalse(apiKey.isBlank());

        System.out.println(
                "Spring Gemini embedding API key is configured. Length = "
                        + apiKey.length()
        );
    }
}