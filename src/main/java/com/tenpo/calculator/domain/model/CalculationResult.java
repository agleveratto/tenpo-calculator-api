package com.tenpo.calculator.domain.model;

public record CalculationResult(
        double num1,
        double num2,
        double percentageApplied,
        double finalResult
) {}