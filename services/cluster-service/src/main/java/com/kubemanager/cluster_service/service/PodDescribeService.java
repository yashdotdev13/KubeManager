package com.kubemanager.cluster_service.service;

import com.kubemanager.cluster_service.dto.response.PodDescribeResponse;

import java.util.UUID;

public interface PodDescribeService {

    PodDescribeResponse describePod(
            UUID clusterId,
            String namespace,
            String podName
    );
}