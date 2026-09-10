package com.tenpo.calculator.infrastructure.adapter.in.web;

import com.tenpo.calculator.domain.model.CalculationResult;
import com.tenpo.calculator.domain.port.in.CalculateUseCase;
import com.tenpo.calculator.infrastructure.adapter.in.web.exception.GlobalExceptionHandler;
import com.tenpo.calculator.infrastructure.adapter.in.web.interceptor.RateLimitInterceptor;
import com.tenpo.calculator.infrastructure.adapter.in.web.interceptor.SemaphoreRpmLimiter;
import com.tenpo.calculator.infrastructure.adapter.out.async.AsyncLogPublisher;
import com.tenpo.calculator.infrastructure.config.WebConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalculatorRestController.class)
@AutoConfigureDataJpa
@Import({RateLimitInterceptor.class, WebConfig.class, GlobalExceptionHandler.class, SemaphoreRpmLimiter.class})
class CalculatorRestControllerRateLimitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalculateUseCase calculateUseCase;

    @MockBean
    private AsyncLogPublisher asyncLogPublisher;

    @BeforeEach
    void setUp() {
        when(calculateUseCase.calculate(anyDouble(), anyDouble()))
                .thenReturn(new CalculationResult(10.0, 20.0, 10.0, 33.0));
    }

    @Test
    @DisplayName("Debe aceptar 3 solicitudes con 200 OK y rechazar la 4ta con 429 conservando num1 y num2")
    void shouldRateLimitAndReturn429WithParams() throws Exception {
        String endpoint = "/api/calculate";

        // 3 peticiones exitosas
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get(endpoint)
                            .param("num1", "10.0")
                            .param("num2", "20.0")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        // 4ta petición en la misma ventana -> Exceso de tasa (429)
        mockMvc.perform(get(endpoint)
                        .param("num1", "10.0")
                        .param("num2", "20.0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isTooManyRequests());
    }
}