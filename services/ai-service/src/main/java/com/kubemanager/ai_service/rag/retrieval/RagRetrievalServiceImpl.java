package com.kubemanager.ai_service.rag.retrieval;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagRetrievalServiceImpl
        implements RagRetrievalService {

    private final VectorStore vectorStore;

    @Override
    public List<Document> retrieve(String query) {

        if (query == null || query.isBlank()) {

            throw new IllegalArgumentException(
                    "RAG query cannot be null or blank."
            );
        }

        String normalizedQuery = query.trim();

        log.debug(
                "Retrieving RAG knowledge for query: {}",
                normalizedQuery
        );

        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(normalizedQuery)
                        .topK(5)
                        .similarityThreshold(0.0)
                        .build()
        );

        if (documents == null || documents.isEmpty()) {

            log.debug(
                    "No relevant RAG documents found for query: {}",
                    normalizedQuery
            );

            return Collections.emptyList();
        }

        log.debug(
                "Retrieved {} RAG documents for query: {}",
                documents.size(),
                normalizedQuery
        );

        return documents;
    }
}