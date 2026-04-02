package com.lexdata.auth.payload.response;

import lombok.Data;
import java.util.List;

@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String refreshToken;
    private Long id;
    private String username;
    private String email;
    private List<String> roles;

    /** Indispensable pour le guard EmailVerifiedGuard côté React. */
    private boolean emailVerified;

    public JwtResponse(String accessToken, String refreshToken, Long id, String username, String email,
            List<String> roles, boolean emailVerified) {
        this.token = accessToken;
        this.refreshToken = refreshToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.emailVerified = emailVerified;
    }
}