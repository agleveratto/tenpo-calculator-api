package com.tenpo.calculator.infrastructure.adapter.out.external;

import com.tenpo.calculator.domain.port.out.DynamicPercentagePort;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PercentageMockAdapter implements DynamicPercentagePort {

    @Override
    @Retry(name = "percentageServiceRetry", fallbackMethod = "getPercentageFallback")
    public double getPercentage() {
        log.info("Llamando al servicio externo de porcentaje...");

        // Simulación: lanzamos excepción para validar que se ejecuten los reintentos
        throw new RuntimeException("Error de conexión con el servicio externo de porcentaje");
    }

    /**
     * Fallback invocado automáticamente por Resilience4j si se agotan todos los reintentos.
     */
    public double getPercentageFallback(Throwable throwable) {
        log.error("Se agotaron los reintentos para obtener el porcentaje. Aplicando fallback. Causa: {}",
                throwable.getMessage());

        // Retorna el porcentaje por defecto
        return 10.0;
    }
}