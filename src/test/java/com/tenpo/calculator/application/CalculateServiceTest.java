package com.tenpo.calculator.application;

import com.tenpo.calculator.application.service.CalculateService;
import com.tenpo.calculator.domain.model.CalculationResult;
import com.tenpo.calculator.domain.port.out.DynamicPercentagePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculateServiceTest {

    @Mock
    private DynamicPercentagePort percentagePort;

    @InjectMocks
    private CalculateService calculateService;

    @Test
    @DisplayName("Debe ejecutar el cálculo obteniendo el porcentaje")
    void shouldCalculateAndSaveLogSuccessfully() {
        double num1 = 5.0;
        double num2 = 5.0;
        double mockPercentage = 10.0;

        when(percentagePort.getPercentage()).thenReturn(mockPercentage);

        CalculationResult result = calculateService.calculate(num1, num2);

        assertNotNull(result);
        assertEquals(5.0, result.num1());
        assertEquals(5.0, result.num2());
        assertEquals(10.0, result.percentageApplied());
        assertEquals(11.0, result.finalResult());

        verify(percentagePort).getPercentage();
    }
}