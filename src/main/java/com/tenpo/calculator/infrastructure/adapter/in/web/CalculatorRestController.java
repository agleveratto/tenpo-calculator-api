package com.tenpo.calculator.infrastructure.adapter.in.web;

import com.tenpo.calculator.domain.model.CalculationResult;
import com.tenpo.calculator.domain.port.in.CalculateUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CalculatorRestController {

    private final CalculateUseCase calculateUseCase;

    public CalculatorRestController(CalculateUseCase calculateUseCase) {
        this.calculateUseCase = calculateUseCase;
    }

    @GetMapping("/calculate")
    public ResponseEntity<CalculationResult> calculate(@RequestParam double num1, @RequestParam double num2) {
        CalculationResult result = calculateUseCase.calculate(num1, num2);
        return ResponseEntity.ok(result);
    }
}
