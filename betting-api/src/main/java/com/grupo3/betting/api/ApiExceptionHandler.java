package com.grupo3.betting.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler({MethodArgumentNotValidException.class, IllegalArgumentException.class})
    ResponseEntity<Map<String,Object>> badRequest(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("timestamp", Instant.now().toString(), "error", "Solicitud inválida", "detail", e.getMessage()));
    }
}
