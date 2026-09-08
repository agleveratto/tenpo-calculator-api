package com.tenpo.calculator.application.service;

import com.tenpo.calculator.domain.model.ApiLog;
import com.tenpo.calculator.domain.port.in.GetHistoryUseCase;
import com.tenpo.calculator.domain.port.out.ApiLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetHistoryService implements GetHistoryUseCase {

    private final ApiLogRepositoryPort logRepositoryPort;

    @Override
    public List<ApiLog> getHistory(int page, int size) {
        log.info("Obteniendo historial de llamadas");
        return logRepositoryPort.findAll(page, size);
    }
}