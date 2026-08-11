package com.kubemanager.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Generic
    BAD_REQUEST("400", "Bad Request"),
    UNAUTHORIZED("401", "Unauthorized"),
    FORBIDDEN("403", "Forbidden"),
    NOT_FOUND("404", "Resource Not Found"),
    CONFLICT("409", "Conflict"),
    VALIDATION_ERROR("422", "Validation Failed"),
    INTERNAL_SERVER_ERROR("500", "Internal Server Error"),

    // Authentication
    AUTHENTICATION_FAILED("AUTH_001", "Authentication Failed"),
    INVALID_TOKEN("AUTH_002", "Invalid Token"),
    TOKEN_EXPIRED("AUTH_003", "Token Expired"),

    // User
    USER_NOT_FOUND("USER_001", "User Not Found"),
    USERNAME_ALREADY_EXISTS("USER_002", "Username Already Exists"),
    EMAIL_ALREADY_EXISTS("USER_003", "Email Already Exists"),

    // Role
    ROLE_NOT_FOUND("ROLE_001", "Role Not Found"),

    // User Profile
    USER_PROFILE_NOT_FOUND("PROFILE_001", "User Profile Not Found"),
    USER_PROFILE_ALREADY_EXISTS("PROFILE_002", "User Profile Already Exists"),
    INVALID_USER_PROFILE("PROFILE_003", "Invalid User Profile"),

    // User Preference
    USER_PREFERENCE_NOT_FOUND("PREFERENCE_001", "User Preference Not Found"),
    INVALID_USER_PREFERENCE("PREFERENCE_002", "Invalid User Preference"),

    INVALID_CLUSTER("CLUSTER_001","Invalid Cluster" ),
    CLUSTER_ALREADY_EXISTS("CLUSTER_002", "Cluster Already Exists"),
    CLUSTER_NOT_FOUND("CLUSTER_003", "Cluster nnt Found"),
    INVALID_CLUSTER_CONFIGURATION("CLUSTER_004", "Invalid Cluster Configuration" ),

    NAMESPACE_ALREADY_EXISTS("NAMESPACE_001", "Namespace Already Exists"),

    INVALID_REQUEST("NAMESPACE_002", "Invalid Request"),

    NAMESPACE_NOT_FOUND("NAMESPACE_003","Namespace Not Found" ),
    NAMESPACE_DELETE_FAILED("NAMESPACE_004","Namespace Delete Failed"),

    NODE_NOT_FOUND("NODE_001", "Node Not Found"),

    POD_NOT_FOUND("POD_001","Pod Not Found"),

    POD_DELETE_FAILED("POD_002","Pod Delete Failed"),

    DEPLOYMENT_NOT_FOUND("DEPLOYMENT_001", "Deployment Not Found"),
    DEPLOYMENT_SCALE_FAILED("DEPLOYMENT_002", "Deployment Scale Failed"),
    DEPLOYMENT_RESTART_FAILED("DEPLOYMENT_003","Deployment Restart Failed"),
    DEPLOYMENT_DELETE_FAILED("DEPLOYMENT_004","Deployment Delete Failed"),
    DEPLOYMENT_ALREADY_EXISTS("DEPLOYMENT_005", "Deployment Already Exists" ),
    DEPLOYMENT_CREATION_FAILED("DEPLOYMENT_006", "Deployment Creation Failed" ),

    SERVICE_ALREADY_EXISTS("SERVICE_001","Service Already Exists" ),

    SERVICE_CREATION_FAILED("SERVICE_002","Service Creation Failed"),

    SERVICE_NOT_FOUND("SERVICE_003","Service Not Found" ),

    INGRESS_CREATION_FAILED("INGRESS_001","Ingress Creation Failed"),
    INGRESS_ALREADY_EXISTS("INGRESS_002", "Ingress Already Exists" ),

    INGRESS_NOT_FOUND("INGRESS_003","Ingress Not Found"),

    CONFIG_MAP_ALREADY_EXISTS("CONFIGMAP_001","ConfigMap Already Exists" ),
    CONFIG_MAP_CREATION_FAILED("CONFIGMAP_002","ConfigMap Creation Failed" ),

    CONFIG_MAP_NOT_FOUND("CONFIGMAP_003","ConfigMap Not Found" ),

    SECRET_ALREADY_EXISTS("SECRET_001","Secret Already Exists"),

    SECRET_CREATION_FAILED("SECRET_003","Secret Creation Failed"),
    SECRET_NOT_FOUND("SECRET_004","Secret Not Found" ),

    PVC_ALREADY_EXISTS("PVC_001","PVC Already Exists" ),
    PVC_CREATION_FAILED("PVC_002","PVC Creation Failed" ),
    PVC_NOT_FOUND("PVE_003","PVC Not Found" ),
    PVC_DELETION_FAILED("PVC_004","PVC Delete Failed" ),

    PV_ALREADY_EXISTS("PV_001","PV Already Exists" ),
    PV_CREATION_FAILED("PV_002","PV Creation Failed" ),
    PV_NOT_FOUND("PV_003","PV Not Found" ),
    PV_DELETION_FAILED("PV_004","PV Delete Failed" ),;

    private final String code;
    private final String message;
}