package com.tenpo.calculator.application.service;

import com.tenpo.calculator.domain.model.CalculationResult;
import com.tenpo.calculator.domain.port.in.CalculateUseCase;
import com.tenpo.calculator.domain.port.out.DynamicPercentagePort;
import org.springframework.stereotype.Service;

@Service
public class CalculateService implements CalculateUseCase {
    private final DynamicPercentagePort percentagePort;

    public CalculateService(DynamicPercentagePort percentagePort) {
        this.percentagePort = percentagePort;
    }

    @Override
    public CalculationResult calculate(double num1, double num2) {
        double percentage = percentagePort.getPercentage();
        double baseSum = num1 + num2;
        double finalResult = baseSum + (baseSum * (percentage / 100.0));

        return new CalculationResult(num1, num2, percentage, finalResult);
    }
}
