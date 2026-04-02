package com.lexdata.auth.exceptions;

import com.lexdata.auth.payload.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({RefreshTokenNotFoundException.class, RefreshTokenExpiredException.class})
    public ResponseEntity<ApiErrorResponse> handleRefreshTokenExceptions(RuntimeException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse(
                        "UNAUTHORIZED",
                        ex.getMessage(),
                        Instant.now().toString(),
                        request.getHeader("X-B3-TraceId") == null ? "" : request.getHeader("X-B3-TraceId")
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        "BAD_REQUEST",
                        ex.getMessage(),
                        Instant.now().toString(),
                        request.getHeader("X-B3-TraceId") == null ? "" : request.getHeader("X-B3-TraceId")
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage() != null
                        ? fieldError.getDefaultMessage()
                        : "Requete invalide.")
                .orElse("Requete invalide.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        "BAD_REQUEST",
                        message,
                        Instant.now().toString(),
                        request.getHeader("X-B3-TraceId") == null ? "" : request.getHeader("X-B3-TraceId")
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        logger.error("Erreur non geree dans auth-service", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(
                        "INTERNAL_ERROR",
                        "Une erreur interne est survenue.",
                        Instant.now().toString(),
                        request.getHeader("X-B3-TraceId") == null ? "" : request.getHeader("X-B3-TraceId")
                ));
    }
}

