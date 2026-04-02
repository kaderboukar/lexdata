package com.lexdata.notifications.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Collections;
import java.util.List;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${lexdata.app.jwtSecret:}")
    private String jwtSecret;

    @PostConstruct
    void validateSecret() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException(
                    "JWT secret manquant. Renseignez la propriété 'lexdata.app.jwtSecret' (env: LEXDATA_JWT_SECRET ou LEXDATA_APP_JWTSECRET).");
        }
        String s = jwtSecret.trim();
        if ((s.length() % 2) != 0) {
            throw new IllegalStateException("JWT secret invalide: longueur hex impaire (attendu: nombre pair de caractères).");
        }
        // 32 bytes min recommandé pour HS256 => 64 chars hex
        if (s.length() < 64) {
            throw new IllegalStateException("JWT secret trop court pour HS256 (attendu >= 64 caractères hex, soit 32 octets).");
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean isHex = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
            if (!isHex) {
                throw new IllegalStateException("JWT secret invalide: contient des caractères non-hexadécimaux.");
            }
        }
        jwtSecret = s;
    }

    private Key key() {
        return Keys.hmacShaKeyFor(hexStringToByteArray(jwtSecret));
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public String getUserNameFromJwtToken(String token) {
        return parseClaims(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Object v = parseClaims(token).get("userId");
        if (v instanceof Integer i) {
            return i.longValue();
        }
        if (v instanceof Long l) {
            return l;
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        if (v instanceof String s && !s.isBlank()) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        List<String> roles = parseClaims(token).get("roles", List.class);
        return roles != null ? roles : Collections.emptyList();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Token JWT invalide: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Token JWT expiré: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT non supporté: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("La chaîne claims JWT est vide: {}", e.getMessage());
        }
        return false;
    }

    public String getSecret() {
        return jwtSecret;
    }
}
