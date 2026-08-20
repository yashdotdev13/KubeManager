package com.kubemanager.ai_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class PodServiceApiResponse {

    private boolean success;

    private String message;

    private List<PodSummaryResponse> data;
}