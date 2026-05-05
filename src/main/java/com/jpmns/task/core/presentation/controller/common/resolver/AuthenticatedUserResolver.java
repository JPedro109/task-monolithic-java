package com.jpmns.task.core.presentation.controller.common.resolver;

import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticatedUserResolver {

    public static String getUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalArgumentException("User is not authenticated");
        }

        if (authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("Principal is null");
        }
        var principal = authentication.getPrincipal();

        return principal.toString();
    }

    public static String getUserIdOrNull() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        var principal = authentication.getPrincipal();
        if (principal == null) {
            return null;
        }

        return principal.toString();
    }
}
