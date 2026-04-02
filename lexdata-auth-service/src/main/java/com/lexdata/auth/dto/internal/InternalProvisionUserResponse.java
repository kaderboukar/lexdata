package com.lexdata.auth.dto.internal;

import java.util.List;

public record InternalProvisionUserResponse(
        Long id,
        String username,
        String email,
        List<String> roles,
        boolean emailVerified,
        boolean active) {
}
