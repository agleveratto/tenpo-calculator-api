package com.tenpo.calculator.infrastructure.adapter.in.web;

import com.tenpo.calculator.domain.model.CalculationResult;
import com.tenpo.calculator.domain.port.in.CalculateUseCase;
import com.tenpo.calculator.infrastructure.adapter.out.async.AsyncLogPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CalculatorRestController {

    private final CalculateUseCase calculateUseCase;
    private final AsyncLogPublisher asyncLogPublisher;

    public CalculatorRestController(CalculateUseCase calculateUseCase, AsyncLogPublisher asyncLogPublisher) {
        this.calculateUseCase = calculateUseCase;
        this.asyncLogPublisher = asyncLogPublisher;
    }

    @GetMapping("/calculate")
    public ResponseEntity<CalculationResult> calculate(@RequestParam double num1, @RequestParam double num2) {
        String endpoint = "/api/calculate";
        String params = String.format("num1=%.2f, num2=%.2f", num1, num2);
        CalculationResult result = calculateUseCase.calculate(num1, num2);
        asyncLogPublisher.publishAsync(endpoint, params, result.toString(), null);
        return ResponseEntity.ok(result);
    }

}
