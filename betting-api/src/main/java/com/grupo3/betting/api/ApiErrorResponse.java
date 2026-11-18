package com.grupo3.betting.api;

import java.time.Instant;
import java.util.Map;

record ApiErrorResponse(Instant timestamp, String error, String detail, Map<String, String> fieldErrors) {
    static ApiErrorResponse of(String error, String detail) {
        return new ApiErrorResponse(Instant.now(), error, detail, Map.of());
    }
    static ApiErrorResponse validation(String detail, Map<String, String> fields) {
        return new ApiErrorResponse(Instant.now(), "Solicitud inválida", detail, Map.copyOf(fields));
    }
}
