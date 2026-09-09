package com.tenpo.calculator.infrastructure.adapter.out.persistence;

import com.tenpo.calculator.domain.model.ApiLog;
import com.tenpo.calculator.domain.port.out.ApiLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ApiLogJpaAdapter implements ApiLogRepositoryPort {

    private final SpringDataJpaLogRepository repository;

    @Override
    public void saveLog(ApiLog entry) {
        ApiLogJpaEntity entity = ApiLogJpaEntity.builder()
                .timestamp(entry.timestamp())
                .endpoint(entry.endpoint())
                .parameters(entry.parameters())
                .response(entry.response())
                .error(entry.error())
                .build();
        repository.save(entity);
        log.info("Llamada registrada");
    }

    @Override
    public List<ApiLog> findAll(int page, int size) {
        log.info("Obteniendo historial de llamadas paginado");
        return repository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
                .getContent()
                .stream()
                .map(e -> new ApiLog(e.getId(), e.getTimestamp(), e.getEndpoint(), e.getParameters(), e.getResponse(), e.getError()))
                .toList();
    }
}