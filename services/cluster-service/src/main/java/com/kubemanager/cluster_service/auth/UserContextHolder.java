package com.kubemanager.cluster_service.auth;

import java.util.UUID;

public final class UserContextHolder {

    private static final ThreadLocal<UserContext> CONTEXT =
            new ThreadLocal<>();

    private UserContextHolder() {
    }

    public static void set(UserContext context) {
        CONTEXT.set(context);
    }

    public static UserContext get() {
        return CONTEXT.get();
    }

    public static UserContext getRequiredContext() {

        UserContext context = CONTEXT.get();

        if (context == null) {
            throw new IllegalStateException("User context is not available.");
        }

        return context;
    }

    public static UUID getCurrentUserId() {
        return getRequiredContext().getUserId();
    }

    public static String getCurrentUsername() {
        return getRequiredContext().getUsername();
    }

    public static String getCurrentEmail() {
        return getRequiredContext().getEmail();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static boolean hasContext() {
        return CONTEXT.get() != null;
    }

}