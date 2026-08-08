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
    DEPLOYMENT_DELETE_FAILED("DEPLOYMENT_004","Deployment Delete Failed"),;

    private final String code;
    private final String message;
}