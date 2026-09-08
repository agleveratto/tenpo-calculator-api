package com.tenpo.calculator.domain.model;

import java.time.LocalDateTime;

public record ApiLog(
        Long id,
        LocalDateTime timestamp,
        String endpoint,
        String parameters,
        String response,
        String error
) {
    public static ApiLog create(String endpoint, String parameters, String response, String error) {
        return new ApiLog(null, LocalDateTime.now(), endpoint, parameters, response, error);
    }
}