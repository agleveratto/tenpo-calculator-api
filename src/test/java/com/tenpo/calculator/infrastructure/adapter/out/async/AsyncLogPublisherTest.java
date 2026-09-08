package com.tenpo.calculator.infrastructure.adapter.out.async;

import com.tenpo.calculator.domain.model.ApiLog;
import com.tenpo.calculator.domain.port.out.ApiLogRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AsyncLogPublisherTest {

    @Mock
    private ApiLogRepositoryPort logRepositoryPort;

    @InjectMocks
    private AsyncLogPublisher asyncLogPublisher;

    @Test
    @DisplayName("publishAsync debe crear el objeto ApiLog y delegar el guardado al puerto logRepositoryPort")
    void shouldCreateAndSaveApiLog() {
        String endpoint = "/api/calculate";
        String params = "num1=5.00, num2=5.00";
        String response = "CalculationResult[num1=5.0, num2=5.0, percentageApplied=10.0, finalResult=11.0]";
        String error = null;

        asyncLogPublisher.publishAsync(endpoint, params, response, error);

        ArgumentCaptor<ApiLog> logCaptor = ArgumentCaptor.forClass(ApiLog.class);
        verify(logRepositoryPort).saveLog(logCaptor.capture());

        ApiLog savedLog = logCaptor.getValue();
        assertThat(savedLog.endpoint()).isEqualTo(endpoint);
        assertThat(savedLog.parameters()).isEqualTo(params);
        assertThat(savedLog.response()).isEqualTo(response);
        assertThat(savedLog.error()).isNull();
        assertThat(savedLog.timestamp()).isNotNull();
    }
}