package com.kubemanager.ai_service.agent.reasoning.rca;


import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RootCauseAnalysis {


    private String summary;

    private String rootCause;

    private List<String> evidence;

    private String confidence;

    private List<String> recommendations;
}
