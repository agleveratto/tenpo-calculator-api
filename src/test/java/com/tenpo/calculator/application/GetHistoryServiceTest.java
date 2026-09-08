package com.tenpo.calculator.application;

import com.tenpo.calculator.application.service.GetHistoryService;
import com.tenpo.calculator.domain.model.ApiLog;
import com.tenpo.calculator.domain.port.out.ApiLogRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetHistoryServiceTest {

    @Mock
    private ApiLogRepositoryPort logRepositoryPort;

    @InjectMocks
    private GetHistoryService getHistoryService;

    @Test
    @DisplayName("Debe retornar la página de historial solicitada")
    void shouldReturnPaginatedHistory() {
        ApiLog mockLog = new ApiLog(1L, LocalDateTime.now(), "/api/calculate", "num1=5, num2=5", "11.0", null);

        when(logRepositoryPort.findAll(0, 10)).thenReturn(List.of(mockLog));

        List<ApiLog> result = getHistoryService.getHistory(0,10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(logRepositoryPort).findAll(0,10);
    }
}