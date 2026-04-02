package com.lexdata.gateway.config;

/**
 * CORS configuration is centralized in
 * SecurityConfig.corsConfigurationSource().
 * A separate CorsWebFilter is NOT needed — having two competing CORS beans
 * causes conflicts and unpredictable behavior in Spring Cloud Gateway
 * (WebFlux).
 * The SecurityConfig bean is the single authoritative source of CORS policy.
 */
public class CorsConfig {
    // Intentionally empty — CORS is handled in SecurityConfig
}
