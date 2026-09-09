package com.tenpo.calculator.infrastructure.adapter.in.web;

import com.tenpo.calculator.domain.limiters.RpmLimiter;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CalculatorRestController.class)
@AutoConfigureDataJpa
@Import({RateLimitInterceptor.class, WebConfig.class, GlobalExceptionHandler.class, SemaphoreRpmLimiter.class})
class CalculatorRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalculateUseCase calculateUseCase;

    @MockBean
    private AsyncLogPublisher asyncLogPublisher;

    @MockBean
    private SemaphoreRpmLimiter rpmLimiter; //

    @BeforeEach
    void setUp() {
        when(rpmLimiter.allowRequest(any())).thenReturn(true);

        when(calculateUseCase.calculate(anyDouble(), anyDouble()))
                .thenReturn(new CalculationResult(10.0, 20.0, 10.0, 33.0));
    }

    @Test
    @DisplayName("GET /api/calculate debe retornar 200 OK con el record del resultado")
    void shouldReturn200AndCalculationResult() throws Exception {
        double num1 = 5.0;
        double num2 = 5.0;
        String endpoint = "/api/calculate";
        String params = String.format("num1=%.2f, num2=%.2f", num1, num2);

        CalculationResult mockResult = new CalculationResult(num1, num2, 10.0, 11.0);

        when(calculateUseCase.calculate(5.0, 5.0)).thenReturn(mockResult);

        doNothing().when(asyncLogPublisher).publishAsync(endpoint, params, mockResult.toString(), null);

        mockMvc.perform(get(endpoint)
                        .param("num1", "5.0")
                        .param("num2", "5.0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.num1").value(5.0))
                .andExpect(jsonPath("$.num2").value(5.0))
                .andExpect(jsonPath("$.percentageApplied").value(10.0))
                .andExpect(jsonPath("$.finalResult").value(11.0));

        verify(calculateUseCase).calculate(5.0, 5.0);
        verify(asyncLogPublisher).publishAsync(endpoint, params, mockResult.toString(), null);
    }


}