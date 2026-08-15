package com.kubemanager.cluster_service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record NodeDrainRequest(

        @Min(1)
        @Max(3600)
        Integer timeoutSeconds,

        @Min(0)
        @Max(3600)
        Integer gracePeriodSeconds,

        Boolean ignoreDaemonSets,

        Boolean deleteEmptyDirData,

        Boolean force

) {

    public int getTimeoutSecondsOrDefault() {
        return timeoutSeconds != null
                ? timeoutSeconds
                : 120;
    }

    public int getGracePeriodSecondsOrDefault() {
        return gracePeriodSeconds != null
                ? gracePeriodSeconds
                : 30;
    }

    public boolean isIgnoreDaemonSets() {
        return ignoreDaemonSets == null || ignoreDaemonSets;
    }

    public boolean isDeleteEmptyDirData() {
        return Boolean.TRUE.equals(deleteEmptyDirData);
    }

    public boolean isForce() {
        return Boolean.TRUE.equals(force);
    }
}