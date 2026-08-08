package com.kubemanager.cluster_service.mapper;


import com.kubemanager.cluster_service.dto.response.ServiceResponse;
import com.kubemanager.cluster_service.dto.response.ServiceSummaryResponse;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class ServiceMapper {

    public ServiceSummaryResponse toSummaryResponse(
            Service service
    ) {

        return ServiceSummaryResponse.builder()
                .name(
                        service.getMetadata().getName()
                )
                .namespace(
                        service.getMetadata().getNamespace()
                )
                .type(
                        service.getSpec().getType()
                )
                .clusterIP(
                        service.getSpec().getClusterIP()
                )
                .port(
                        getPort(service)
                )
                .build();
    }

    public ServiceResponse toResponse(
            Service service
    ) {

        return ServiceResponse.builder()
                .name(
                        service.getMetadata().getName()
                )
                .namespace(
                        service.getMetadata().getNamespace()
                )
                .type(
                        service.getSpec().getType()
                )
                .clusterIP(
                        service.getSpec().getClusterIP()
                )
                .port(
                        getPort(service)
                )
                .targetPort(
                        getTargetPort(service)
                )
                .nodePort(
                        getNodePort(service)
                )
                .selector(
                        service.getSpec().getSelector()
                )
                .creationTimestamp(
                        service.getMetadata().getCreationTimestamp() != null
                                ? OffsetDateTime.parse(
                                service.getMetadata().getCreationTimestamp()
                        )
                                : null
                )
                .build();
    }

    private Integer getPort(
            Service service
    ) {

        if (service.getSpec().getPorts() == null ||
                service.getSpec().getPorts().isEmpty()) {

            return null;
        }

        ServicePort port = service.getSpec().getPorts().getFirst();

        return port.getPort();
    }

    private Integer getTargetPort(
            Service service
    ) {

        if (service.getSpec().getPorts() == null ||
                service.getSpec().getPorts().isEmpty()) {

            return null;
        }

        ServicePort port = service.getSpec().getPorts().getFirst();

        if (port.getTargetPort() == null) {
            return null;
        }

        return port.getTargetPort().getIntVal();
    }

    private Integer getNodePort(
            Service service
    ) {

        if (service.getSpec().getPorts() == null ||
                service.getSpec().getPorts().isEmpty()) {

            return null;
        }

        ServicePort port = service.getSpec().getPorts().getFirst();

        return port.getNodePort();
    }
}
