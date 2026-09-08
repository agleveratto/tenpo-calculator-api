package com.tenpo.calculator.infrastructure.adapter.out.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PercentageMockAdapterTest {

    private final PercentageMockAdapter adapter = new PercentageMockAdapter();

    @Test
    @DisplayName("Debe retornar un porcentaje por defecto del 10%")
    void shouldReturnDefaultPercentage() {
        double percentage = adapter.getPercentage();

        assertEquals(10.0, percentage, "El porcentaje retornado por el adapter mock debe ser 10.0");
    }
}