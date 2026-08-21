package com.kubemanager.ai_service.agent.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryExtractionResult {

    private boolean shouldRemember;

    private List<MemoryItem> memories;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemoryItem {

        private String memoryType;

        private String content;

        private String source;
    }
}