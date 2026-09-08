package com.tenpo.calculator.infrastructure.adapter.out.async;

import com.tenpo.calculator.domain.model.ApiLog;
import com.tenpo.calculator.domain.port.out.ApiLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncLogPublisher {

    private final ApiLogRepositoryPort logRepositoryPort;

    @Async
    public void publishAsync(String endpoint, String params, String response, String error) {
        log.info("Registrando llamada de manera asincrona");
        ApiLog entry = ApiLog.create(endpoint, params, response, error);
        logRepositoryPort.saveLog(entry);
    }
}