package com.kubemanager.ai_service.rag;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class RagDocument {


    private final String content;

    private final String source;

    private final String documentType;

    private final Map<String, Object> metadata;
}
