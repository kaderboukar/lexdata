package com.lexdata.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {

                // 1. On récupère qui est connecté via le Token
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                // 2. EXTRACTION DES RÔLES
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(hexStringToByteArray(jwtUtils.getSecret()))
                        .build()
                        .parseClaimsJws(jwt)
                        .getBody();

                List<String> rolesStr = claims.get("roles", List.class);
                var authorities = rolesStr != null ? rolesStr.stream()
                        .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
                        : new java.util.ArrayList<org.springframework.security.core.authority.SimpleGrantedAuthority>();

                // 3. Création de l'authentification
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 4. On ouvre la porte
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Impossible d'authentifier l'utilisateur via JWT: {}", e.getMessage());
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