package com.grupo3.betting.api.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.access.AccessDeniedException;

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(com.grupo3.betting.api.recharge.PaymentGatewayUnavailableException.class)
    ResponseEntity<ApiErrorResponse> paymentUnavailable(RuntimeException exception) {
        return ResponseEntity.status(503).body(ApiErrorResponse.of("Pasarela no disponible", exception.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error -> fields.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(ApiErrorResponse.validation("Revisa los campos indicados", fields));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiErrorResponse> badRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(ApiErrorResponse.of("Solicitud inválida", exception.getMessage()));
    }
    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<ApiErrorResponse> conflict(IllegalStateException exception) {
        return ResponseEntity.status(409).body(ApiErrorResponse.of("No se pudo completar", exception.getMessage()));
    }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiErrorResponse> forbidden(AccessDeniedException exception) {
        return ResponseEntity.status(403).body(ApiErrorResponse.of("Acceso denegado", "No tienes permisos para esta acción"));
    }
}
