package com.lexdata.synthese.security;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${lexdata.app.jwtSecret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                // 1. Extraction du username
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                logger.info("Tentative d'authentification pour l'utilisateur: {}", username);

                // 2. EXTRACTION DES RÔLES
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(hexStringToByteArray(jwtSecret))
                        .build()
                        .parseClaimsJws(jwt)
                        .getBody();

                List<String> rolesStr = claims.get("roles", List.class);
                logger.info("Rôles extraits du JWT: {}", rolesStr);

                var authorities = rolesStr != null ? rolesStr.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()) : new java.util.ArrayList<SimpleGrantedAuthority>();

                // 3. Création de l'auth avec les rôles
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username,
                        null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("Authentification définie dans le SecurityContext pour user: {}", username);
            } else if (jwt != null) {
                logger.warn("JWT présent mais invalide pour cette requête");
            }
        } catch (Exception e) {
            logger.error("Impossible d'authentifier l'utilisateur: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
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
}
