package com.lexdata.gateway.config;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${lexdata.jwtSecret}")
    private String jwtSecretHex;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public ReactiveJwtAuthenticationConverter reactiveJwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter granted = new JwtGrantedAuthoritiesConverter();
        granted.setAuthoritiesClaimName("roles");
        granted.setAuthorityPrefix("");
        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> Flux.fromIterable(granted.convert(jwt)));
        return converter;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            ReactiveJwtDecoder jwtDecoder,
            ReactiveJwtAuthenticationConverter reactiveJwtAuthenticationConverter) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                // .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchanges -> exchanges
                        // Allow OPTIONS preflight requests
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        // Public endpoints (Auth, Swagger, etc.)
                        .pathMatchers("/api/auth/**").permitAll()
                        .pathMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .pathMatchers("/api/juridique/textes/**").permitAll()
                        // All other endpoints require authentication
                        .anyExchange().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((exchange, ex) -> writeErrorResponse(
                                exchange.getResponse(),
                                HttpStatus.UNAUTHORIZED,
                                "UNAUTHORIZED",
                                "Authentification requise.",
                                exchange.getRequest().getHeaders().getFirst("X-B3-TraceId")
                        ))
                        .accessDeniedHandler((exchange, ex) -> writeErrorResponse(
                                exchange.getResponse(),
                                HttpStatus.FORBIDDEN,
                                "FORBIDDEN",
                                "Acces refuse.",
                                exchange.getRequest().getHeaders().getFirst("X-B3-TraceId")
                        )))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtDecoder(jwtDecoder)
                                .jwtAuthenticationConverter(reactiveJwtAuthenticationConverter)))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; script-src 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline';"))
                        .hsts(hsts -> hsts.maxAge(java.time.Duration.ofDays(365)))
                        .frameOptions(frameOptions -> frameOptions.mode(
                                org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.Mode.DENY))
                        .xssProtection(ServerHttpSecurity.HeaderSpec.XssProtectionSpec::disable));

        return http.build();
    }

    private Mono<Void> writeErrorResponse(org.springframework.http.server.reactive.ServerHttpResponse response,
                                          HttpStatus status,
                                          String code,
                                          String message,
                                          String traceId) {
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = Map.of(
                "code", code,
                "message", message,
                "timestamp", Instant.now().toString(),
                "traceId", traceId == null ? "" : traceId
        );
        try {
            byte[] bytes = objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return response.setComplete();
        }
    }

    // @Bean
    // public CorsConfigurationSource corsConfigurationSource() {
    //     CorsConfiguration configuration = new CorsConfiguration();
    //     configuration.setAllowedOrigins(Arrays.asList("http://localhost:80"));
    //     configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    //     configuration.setAllowedHeaders(Arrays.asList("*"));
    //     configuration.setAllowCredentials(true);
    //     configuration.setExposedHeaders(Arrays.asList("Authorization"));

    //     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //     source.registerCorsConfiguration("/**", configuration);
    //     return source;
    // }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        byte[] secretBytes = hexStringToByteArray(jwtSecretHex);
        SecretKeySpec secretKey = new SecretKeySpec(secretBytes, "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
