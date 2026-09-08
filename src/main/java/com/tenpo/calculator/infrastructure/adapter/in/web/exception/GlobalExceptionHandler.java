package com.tenpo.calculator.infrastructure.adapter.in.web.exception;

import com.tenpo.calculator.domain.model.CalculationResult;
import com.tenpo.calculator.infrastructure.adapter.out.async.AsyncLogPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final AsyncLogPublisher asyncLogPublisher;

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<?> handleRateLimitExceeded(RateLimitExceededException ex) {
        // Registrar intento fallido en el audit log
        asyncLogPublisher.publishAsync(ex.getPath(), "Rate limit exceeded", null, ex.getMessage());

        // Si la petición vino de /api/history, devolvemos una lista vacía o un mapa estructurado de error
        if (ex.getPath().contains("/history")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Collections.emptyList());
        }

        // Si vino de /api/calculate, mantenemos la estructura de CalculationResult
        CalculationResult errorResult = new CalculationResult(ex.getNum1(), ex.getNum2(), 0.0, 0.0);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(errorResult);
    }
}