package com.tenpo.calculator.infrastructure.adapter.in.web;

import com.tenpo.calculator.domain.model.ApiLog;
import com.tenpo.calculator.domain.port.in.GetHistoryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "History", description = "Consulta de historial")
public class HistoryRestController {

    private final GetHistoryUseCase getHistoryUseCase;

    public HistoryRestController(GetHistoryUseCase getHistoryUseCase) {
        this.getHistoryUseCase = getHistoryUseCase;
    }

    @Operation(summary = "Consultar el historial de llamadas",
            description = "Obtiene una lista paginada de los registros de auditoría almacenados en la base de datos. Posee un límite independiente de 3 solicitudes por minuto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Historial obtenido exitosamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ApiLog.class)))),
            @ApiResponse(responseCode = "429",
                    description = "Rate limit excedido",
                    content = @Content(mediaType = "text/plain"))
    })
    @GetMapping("/history")
    public ResponseEntity<List<ApiLog>> getHistory(@Parameter(description = "Número de página a consultar (comienza en 0)", example = "0")
                                                       @RequestParam(defaultValue = "0") int page,
                                                   @Parameter(description = "Cantidad de registros por página", example = "10")
                                                   @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(getHistoryUseCase.getHistory(page, size));
    }
}
