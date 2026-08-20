package com.kubemanager.ai_service.dto;


import lombok.Data;

@Data
public class PodInfoServiceApiResponse {

    private boolean success;

    private String message;

    private PodResponse data;
}