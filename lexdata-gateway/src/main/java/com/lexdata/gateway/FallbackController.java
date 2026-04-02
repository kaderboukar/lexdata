package com.lexdata.gateway;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {

    @GetMapping("/fallback/user")
    public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback(
            @RequestHeader(name = "X-B3-TraceId", required = false) String traceId) {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "code", "DOWNSTREAM_SERVICE_UNAVAILABLE",
                "message", "Service utilisateur momentanement indisponible. Veuillez reessayer plus tard.",
                "timestamp", Instant.now().toString(),
                "traceId", traceId == null ? "" : traceId)));
    }

    @GetMapping("/fallback/default")
    public Mono<ResponseEntity<Map<String, Object>>> defaultFallback(
            @RequestHeader(name = "X-B3-TraceId", required = false) String traceId) {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "code", "DOWNSTREAM_SERVICE_UNAVAILABLE",
                "message", "Service temporairement indisponible. Veuillez reessayer plus tard.",
                "timestamp", Instant.now().toString(),
                "traceId", traceId == null ? "" : traceId)));
    }
}
