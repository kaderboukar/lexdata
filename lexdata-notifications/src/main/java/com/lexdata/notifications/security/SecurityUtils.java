package com.lexdata.notifications.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Identifiant auth (claim {@code userId}) — peut être absent sur d'anciens JWT.
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getDetails() == null) {
            return null;
        }
        if (auth.getDetails() instanceof Map<?, ?> m) {
            Object v = m.get("userId");
            if (v instanceof Long l) {
                return l;
            }
            if (v instanceof Number n) {
                return n.longValue();
            }
            if (v instanceof String s && !s.isBlank()) {
                try {
                    return Long.parseLong(s);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }
}
