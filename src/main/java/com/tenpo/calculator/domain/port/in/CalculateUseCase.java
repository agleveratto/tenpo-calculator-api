package com.tenpo.calculator.domain.port.in;

import com.tenpo.calculator.domain.model.CalculationResult;

public interface CalculateUseCase {
    CalculationResult calculate(double num1, double num2);
}
