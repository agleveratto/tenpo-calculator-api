package com.tenpo.calculator.infrastructure.adapter.out.persistence.web;

import com.tenpo.calculator.domain.model.ApiLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiLogJpaAdapterTest {

    @Mock
    private SpringDataJpaLogRepository repository;

    @InjectMocks
    private ApiLogJpaAdapter apiLogJpaAdapter;

    @Test
    @DisplayName("saveLog debe mapear ApiLog a ApiLogJpaEntity y llamar a repository.save")
    void shouldMapToEntityAndSave() {
        LocalDateTime now = LocalDateTime.now();
        ApiLog entry = new ApiLog( 1L, now, "/api/calculate", "num1=5.00, num2=5.00", "Result", null);

        apiLogJpaAdapter.saveLog(entry);

        ArgumentCaptor<ApiLogJpaEntity> entityCaptor = ArgumentCaptor.forClass(ApiLogJpaEntity.class);
        verify(repository).save(entityCaptor.capture());

        ApiLogJpaEntity savedEntity = entityCaptor.getValue();
        assertThat(savedEntity.getTimestamp()).isEqualTo(now);
        assertThat(savedEntity.getEndpoint()).isEqualTo("/api/calculate");
        assertThat(savedEntity.getParameters()).isEqualTo("num1=5.00, num2=5.00");
        assertThat(savedEntity.getResponse()).isEqualTo("Result");
        assertThat(savedEntity.getError()).isNull();
    }

    @Test
    @DisplayName("findAll debe consultar el repositorio con paginación/ordenamiento y mapear las entidades a ApiLog")
    void shouldFindAllPagedAndSorted() {
        // Arrange
        int page = 0;
        int size = 10;
        LocalDateTime now = LocalDateTime.now();

        ApiLogJpaEntity entity1 = ApiLogJpaEntity.builder()
                .id(1L)
                .timestamp(now)
                .endpoint("/api/calculate")
                .parameters("num1=5.00, num2=5.00")
                .response("Result OK")
                .error(null)
                .build();

        ApiLogJpaEntity entity2 = ApiLogJpaEntity.builder()
                .id(2L)
                .timestamp(now.minusMinutes(5))
                .endpoint("/api/calculate")
                .parameters("num1=10.00, num2=20.00")
                .response(null)
                .error("Error en servicio externo")
                .build();

        List<ApiLogJpaEntity> entities = List.of(entity1, entity2);
        PageImpl<ApiLogJpaEntity> pageResult = new PageImpl<>(entities);

        when(repository.findAll(any(Pageable.class))).thenReturn(pageResult);

        // Act
        List<ApiLog> result = apiLogJpaAdapter.findAll(page, size);

        // Assert: Validar mapeo de resultados
        assertThat(result).hasSize(2);

        assertThat(result.getFirst().id()).isEqualTo(1L);
        assertThat(result.get(0).endpoint()).isEqualTo("/api/calculate");
        assertThat(result.get(0).response()).isEqualTo("Result OK");

        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).error()).isEqualTo("Error en servicio externo");

        // Assert: Capturar y verificar los parámetros de paginación pasados al repositorio
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(page);
        assertThat(pageable.getPageSize()).isEqualTo(size);
        assertThat(pageable.getSort().getOrderFor("id")).isNotNull();
        assertThat(Objects.requireNonNull(pageable.getSort().getOrderFor("id")).isDescending()).isTrue();
    }

    @Test
    @DisplayName("findAll debe retornar una lista vacía cuando el repositorio no devuelve registros")
    void shouldReturnEmptyListWhenNoLogsFound() {
        // Arrange
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        // Act
        List<ApiLog> result = apiLogJpaAdapter.findAll(0, 10);

        // Assert
        assertThat(result).isEmpty();
        verify(repository).findAll(any(Pageable.class));
    }
}