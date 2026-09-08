package com.tenpo.calculator.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalculationResultTest {

    @Test
    @DisplayName("Debe instanciar correctamente el record de resultado de cálculo")
    void shouldCreateCalculationResultRecord() {
        double num1 = 5.0;
        double num2 = 10.0;
        double percentage = 10.0;
        double finalResult = 16.5;

        CalculationResult result = new CalculationResult(num1, num2, percentage, finalResult);

        assertEquals(5.0, result.num1());
        assertEquals(10.0, result.num2());
        assertEquals(10.0, result.percentageApplied());
        assertEquals(16.5, result.finalResult());
    }

    @Test
    @DisplayName("Debe validar la igualdad estructural (equals/hashCode) inherente al record")
    void shouldEnforceRecordEquality() {
        CalculationResult result1 = new CalculationResult(5.0, 5.0, 10.0, 11.0);
        CalculationResult result2 = new CalculationResult(5.0, 5.0, 10.0, 11.0);

        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
    }
}