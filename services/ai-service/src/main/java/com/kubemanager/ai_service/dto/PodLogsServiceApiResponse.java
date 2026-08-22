package com.kubemanager.ai_service.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PodLogsServiceApiResponse {

    private boolean success;

    private String message;

    private PodLogsResponse data;
}