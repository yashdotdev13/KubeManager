package com.kubemanager.ai_service.rag.retrieval;

import org.springframework.ai.document.Document;

import java.util.List;

public interface RagRetrievalService {

    List<Document> retrieve(String query);
}
