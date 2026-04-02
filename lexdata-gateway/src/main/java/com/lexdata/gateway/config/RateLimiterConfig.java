package com.lexdata.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Configuration
public class RateLimiterConfig {

    @Bean
    @Primary
    public KeyResolver keyResolver() {
        // Limit by authenticated user (Principal name)
        // If not authenticated, limit by IP address
        return exchange -> exchange.getPrincipal()
                .map(Principal::getName)
                .switchIfEmpty(Mono.justOrEmpty(exchange.getRequest().getRemoteAddress())
                        .map(addr -> addr.getAddress().getHostAddress())
                        .defaultIfEmpty("unknown"));
    }
}
