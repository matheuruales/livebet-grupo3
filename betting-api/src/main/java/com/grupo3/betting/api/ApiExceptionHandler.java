package com.grupo3.betting.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;
import org.springframework.security.access.AccessDeniedException;

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler({MethodArgumentNotValidException.class, IllegalArgumentException.class})
    ResponseEntity<Map<String,Object>> badRequest(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("timestamp", Instant.now().toString(), "error", "Solicitud inválida", "detail", e.getMessage()));
    }
    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<Map<String,Object>> conflict(IllegalStateException e) {
        return ResponseEntity.status(409).body(Map.of("timestamp", Instant.now().toString(), "error", "No se pudo completar", "detail", e.getMessage()));
    }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Map<String,Object>> forbidden(AccessDeniedException e) {
        return ResponseEntity.status(403).body(Map.of("timestamp", Instant.now().toString(), "error", "Acceso denegado", "detail", "No tienes permisos para esta acción"));
    }
}
