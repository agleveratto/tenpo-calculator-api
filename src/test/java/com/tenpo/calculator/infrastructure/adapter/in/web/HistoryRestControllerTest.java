package com.tenpo.calculator.infrastructure.adapter.in.web;

import com.tenpo.calculator.domain.model.ApiLog;
import com.tenpo.calculator.domain.port.in.GetHistoryUseCase;
import com.tenpo.calculator.infrastructure.adapter.in.web.exception.GlobalExceptionHandler;
import com.tenpo.calculator.infrastructure.adapter.in.web.interceptor.RateLimitInterceptor;
import com.tenpo.calculator.infrastructure.adapter.in.web.interceptor.SemaphoreRpmLimiter;
import com.tenpo.calculator.infrastructure.adapter.out.async.AsyncLogPublisher;
import com.tenpo.calculator.infrastructure.config.WebConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HistoryRestController.class)
@AutoConfigureDataJpa
@Import({RateLimitInterceptor.class, WebConfig.class, GlobalExceptionHandler.class, SemaphoreRpmLimiter.class})
class HistoryRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetHistoryUseCase getHistoryUseCase;

    @MockBean
    private AsyncLogPublisher asyncLogPublisher;

    @MockBean
    private SemaphoreRpmLimiter rpmLimiter; // <-- Mockeamos el limitador para que nunca bloquee por 429

    @BeforeEach
    void setUp() {
        // Por defecto, permitimos todas las peticiones en este test
        when(rpmLimiter.allowRequest(any(), any())).thenReturn(true);
    }

    @Test
    void shouldReturn200AndEmptyHistory() throws Exception {
        List<ApiLog> emptyList = List.of();
        when(getHistoryUseCase.getHistory(0, 10)).thenReturn(emptyList);

        String endpoint = "/api/history";
        mockMvc.perform(get(endpoint)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(getHistoryUseCase).getHistory(0, 10);
    }

    @Test
    void shouldReturn200AndApiLogHistory() throws Exception {
        String endpoint = "/api/history";
        ApiLog mockLog = new ApiLog(1L, LocalDateTime.now(), "/api/calculate", "num1=5, num2=5", "11.0", null);
        List<ApiLog> logList = List.of(mockLog);
        when(getHistoryUseCase.getHistory(0, 10)).thenReturn(logList);

        mockMvc.perform(get(endpoint)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$[0].endpoint").value("/api/calculate"))
                .andExpect(jsonPath("$[0].parameters").value("num1=5, num2=5"))
                .andExpect(jsonPath("$[0].response").value("11.0"))
                .andExpect(jsonPath("$[0].error").isEmpty());

        verify(getHistoryUseCase).getHistory(0, 10);
    }
}