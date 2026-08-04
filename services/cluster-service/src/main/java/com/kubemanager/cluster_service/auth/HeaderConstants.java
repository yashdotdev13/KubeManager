package com.kubemanager.cluster_service.auth;

public final class HeaderConstants {

    private HeaderConstants() {
    }

    public static final String USER_ID = "X-User-Id";
    public static final String USERNAME = "X-Username";
    public static final String EMAIL = "X-Email";
    public static final String ROLES = "X-Roles";
    public static final String PROVIDER = "X-Provider";
    public static final String REQUEST_ID = "X-Request-Id";
    public static final String CORRELATION_ID = "X-Correlation-Id";

}