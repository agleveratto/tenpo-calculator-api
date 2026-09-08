package com.tenpo.calculator.infrastructure.adapter.out.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PercentageMockAdapterTest {

    @Autowired
    private PercentageMockAdapter adapter;

    @Test
    @DisplayName("Debe agotar los reintentos y retornar el valor del fallback cuando el método falla")
    void shouldExecuteRetryAndTriggerFallback() {
        double result = adapter.getPercentage();

        assertEquals(10.0, result, "Debe retornar 10.0 provisto por el fallback tras agotar los reintentos");
    }
}