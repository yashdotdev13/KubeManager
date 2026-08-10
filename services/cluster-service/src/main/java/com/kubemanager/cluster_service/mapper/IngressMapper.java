package com.kubemanager.cluster_service.mapper;

import com.kubemanager.cluster_service.dto.response.IngressResponse;
import com.kubemanager.cluster_service.dto.response.IngressSummaryResponse;
import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRule;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class IngressMapper {

    public IngressSummaryResponse toSummaryResponse(
            Ingress ingress
    ) {

        return IngressSummaryResponse.builder()
                .name(
                        ingress.getMetadata().getName()
                )
                .namespace(
                        ingress.getMetadata().getNamespace()
                )
                .host(
                        getHost(ingress)
                )
                .address(
                        getAddress(ingress)
                )
                .build();
    }

    public IngressResponse toResponse(
            Ingress ingress
    ) {

        return IngressResponse.builder()
                .name(
                        ingress.getMetadata().getName()
                )
                .namespace(
                        ingress.getMetadata().getNamespace()
                )
                .host(
                        getHost(ingress)
                )
                .path(
                        getPath(ingress)
                )
                .serviceName(
                        getServiceName(ingress)
                )
                .servicePort(
                        getServicePort(ingress)
                )
                .address(
                        getAddress(ingress)
                )
                .creationTimestamp(
                        ingress.getMetadata().getCreationTimestamp() != null
                                ? OffsetDateTime.parse(
                                ingress.getMetadata().getCreationTimestamp()
                        )
                                : null
                )
                .build();
    }

    private String getHost(
            Ingress ingress
    ) {

        if (ingress.getSpec() == null ||
                ingress.getSpec().getRules() == null ||
                ingress.getSpec().getRules().isEmpty()) {

            return null;
        }

        IngressRule rule =
                ingress.getSpec().getRules().getFirst();

        return rule.getHost();
    }

    private String getPath(
            Ingress ingress
    ) {

        if (ingress.getSpec() == null ||
                ingress.getSpec().getRules() == null ||
                ingress.getSpec().getRules().isEmpty()) {

            return null;
        }

        IngressRule rule =
                ingress.getSpec().getRules().getFirst();

        if (rule.getHttp() == null ||
                rule.getHttp().getPaths() == null ||
                rule.getHttp().getPaths().isEmpty()) {

            return null;
        }

        HTTPIngressPath path =
                rule.getHttp().getPaths().getFirst();

        return path.getPath();
    }

    private String getServiceName(
            Ingress ingress
    ) {

        if (ingress.getSpec() == null ||
                ingress.getSpec().getRules() == null ||
                ingress.getSpec().getRules().isEmpty()) {

            return null;
        }

        IngressRule rule =
                ingress.getSpec().getRules().getFirst();

        if (rule.getHttp() == null ||
                rule.getHttp().getPaths() == null ||
                rule.getHttp().getPaths().isEmpty()) {

            return null;
        }

        HTTPIngressPath path =
                rule.getHttp().getPaths().getFirst();

        if (path.getBackend() == null ||
                path.getBackend().getService() == null) {

            return null;
        }

        return path.getBackend()
                .getService()
                .getName();
    }

    private Integer getServicePort(
            Ingress ingress
    ) {

        if (ingress.getSpec() == null ||
                ingress.getSpec().getRules() == null ||
                ingress.getSpec().getRules().isEmpty()) {

            return null;
        }

        IngressRule rule =
                ingress.getSpec().getRules().getFirst();

        if (rule.getHttp() == null ||
                rule.getHttp().getPaths() == null ||
                rule.getHttp().getPaths().isEmpty()) {

            return null;
        }

        HTTPIngressPath path =
                rule.getHttp().getPaths().getFirst();

        if (path.getBackend() == null ||
                path.getBackend().getService() == null ||
                path.getBackend().getService().getPort() == null) {

            return null;
        }

        return path.getBackend()
                .getService()
                .getPort()
                .getNumber();
    }

    private String getAddress(Ingress ingress) {

        if (ingress.getStatus() == null ||
                ingress.getStatus().getLoadBalancer() == null ||
                ingress.getStatus().getLoadBalancer().getIngress() == null ||
                ingress.getStatus().getLoadBalancer().getIngress().isEmpty()) {

            return null;
        }

        var ingressStatus = ingress.getStatus()
                .getLoadBalancer()
                .getIngress()
                .getFirst();

        return ingressStatus.getIp() != null
                ? ingressStatus.getIp()
                : ingressStatus.getHostname();
    }
}