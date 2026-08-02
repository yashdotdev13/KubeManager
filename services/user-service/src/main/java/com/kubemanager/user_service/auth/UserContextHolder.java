package com.kubemanager.user_service.auth;


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

    public static void clear() {
        CONTEXT.remove();
    }

    public static boolean hasContext() {
        return CONTEXT.get() != null;
    }

}