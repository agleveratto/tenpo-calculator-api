package com.tenpo.calculator.infrastructure.adapter.in.web;

import com.tenpo.calculator.domain.model.CalculationResult;
import com.tenpo.calculator.domain.port.in.CalculateUseCase;
import com.tenpo.calculator.infrastructure.adapter.out.async.AsyncLogPublisher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Realizar un cálculo matemático", description = "Toma dos números, aplica un porcentaje dinámico y registra la auditoría de forma asíncrona. Posee un límite de 3 solicitudes por minuto (Rate Limiting).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cálculo exitoso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CalculationResult.class))),
            @ApiResponse(responseCode = "429", description = "Rate limit excedido (más de 3 solicitudes por minuto)",
                    content = @Content(mediaType = "text/plain"))
    })
    @GetMapping("/calculate")
    public ResponseEntity<CalculationResult> calculate(@Parameter(description = "Primer número para el cálculo", example = "5.0") @RequestParam double num1,
                                                       @Parameter(description = "Segundo número para el cálculo", example = "10.0") @RequestParam double num2) {
        String endpoint = "/api/calculate";
        String params = String.format("num1=%.2f, num2=%.2f", num1, num2);
        CalculationResult result = calculateUseCase.calculate(num1, num2);
        asyncLogPublisher.publishAsync(endpoint, params, result.toString(), null);
        return ResponseEntity.ok(result);
    }

}
