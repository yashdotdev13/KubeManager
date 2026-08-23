package com.kubemanager.ai_service.rag.ingestion;


import com.kubemanager.ai_service.rag.RagDocument;

public interface RagIngestionService {

    void ingest(RagDocument document);
}
