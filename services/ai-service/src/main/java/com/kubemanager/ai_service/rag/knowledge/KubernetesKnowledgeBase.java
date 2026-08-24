package com.kubemanager.ai_service.rag.knowledge;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KubernetesKnowledgeBase {

    public List<String> getDocuments() {

        return List.of(

                """
                Kubernetes Pod Lifecycle

                A Kubernetes Pod is the smallest deployable unit
                in Kubernetes. A Pod can contain one or more
                containers that share networking and storage.

                A Pod can have the following lifecycle phases:
                Pending, Running, Succeeded, Failed, and Unknown.

                Pending means the Pod has been accepted by Kubernetes
                but one or more containers have not yet started.

                Running means the Pod has been bound to a node and
                at least one container is running or starting.

                Succeeded means all containers terminated successfully.

                Failed means all containers terminated and at least
                one container terminated unsuccessfully.

                Unknown means Kubernetes cannot determine the Pod state.
                """,

                """
                Kubernetes CrashLoopBackOff

                CrashLoopBackOff occurs when a container repeatedly
                starts and terminates unsuccessfully.

                Common causes include application crashes, invalid
                configuration, missing environment variables,
                unavailable dependencies, incorrect startup commands,
                failed health checks, and missing configuration.

                Troubleshooting should begin by checking Pod status,
                container logs, previous container logs, Kubernetes
                events, configuration, environment variables,
                health probes, and dependent services.

                The kubectl describe pod command provides information
                about container state, events, scheduling, probes,
                volumes, and configuration.

                The kubectl logs command can be used to inspect
                container output. The --previous option can be used
                to retrieve logs from a previous container instance.
                """,

                """
                Kubernetes Container Logs

                Container logs are an important source of information
                when troubleshooting Kubernetes workloads.

                The kubectl logs command retrieves logs from a container.

                When a container has restarted, previous logs can be
                retrieved using kubectl logs --previous.

                For Pods containing multiple containers, the container
                must be specified when retrieving logs.

                Logs should be correlated with Pod status and Kubernetes
                events when investigating failures.
                """,

                """
                Kubernetes Events

                Kubernetes Events provide information about important
                changes and failures occurring in the cluster.

                Events can provide information about scheduling failures,
                image pulling, container creation, probe failures,
                volume mounting problems, and other lifecycle events.

                Events are especially useful when a Pod cannot start,
                cannot be scheduled, or repeatedly fails.

                Kubernetes events should be considered together with
                Pod status, container logs, and resource configuration.
                """,

                """
                Kubernetes Health Probes

                Kubernetes supports startup probes, readiness probes,
                and liveness probes.

                A startup probe is useful for applications that require
                significant startup time. It prevents liveness and
                readiness probes from running too early.

                A readiness probe determines whether a container is
                ready to receive traffic. A failed readiness probe
                causes the Pod to be removed from the endpoints of
                the associated Service.

                A liveness probe determines whether a container is
                still healthy. Repeated liveness probe failures can
                cause Kubernetes to restart the container.

                Incorrect probe configuration can cause unexpected
                restarts or make a healthy application unavailable.
                """,

                """
                Kubernetes ConfigMaps and Secrets

                ConfigMaps provide a mechanism for storing
                non-sensitive configuration data.

                Secrets are intended for sensitive configuration such
                as passwords, tokens, credentials, and certificates.

                Applications can consume ConfigMaps and Secrets through
                environment variables or mounted volumes.

                Missing ConfigMaps or Secrets can prevent containers
                from starting correctly and may contribute to
                CrashLoopBackOff conditions.
                """,

                """
                Kubernetes CPU and Memory Resources

                Kubernetes resource requests and limits control how
                containers consume CPU and memory.

                Resource requests influence Pod scheduling because
                Kubernetes uses requested resources when selecting
                a suitable node.

                Resource limits constrain the maximum resources a
                container can consume.

                Incorrect memory configuration can cause containers
                to be terminated because of out-of-memory conditions.

                Resource problems should be investigated together with
                Pod status, container logs, and node capacity.
                """,

                """
                Kubernetes Services and Networking

                Kubernetes Services provide stable networking endpoints
                for applications running inside a cluster.

                Services select Pods using labels and forward traffic
                to matching endpoints.

                When a Pod cannot communicate with another Service,
                investigate Service selectors, EndpointSlices,
                DNS resolution, NetworkPolicies, container ports,
                and application listeners.

                A Service may exist while still having no usable
                endpoints if its selector does not match the expected
                Pods.
                """
        );
    }
}
