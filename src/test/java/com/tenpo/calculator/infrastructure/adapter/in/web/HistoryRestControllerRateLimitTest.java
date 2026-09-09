package com.tenpo.calculator.infrastructure.adapter.in.web;

import com.tenpo.calculator.domain.port.in.GetHistoryUseCase;
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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HistoryRestController.class)
@AutoConfigureDataJpa
@Import({RateLimitInterceptor.class, WebConfig.class, GlobalExceptionHandler.class, SemaphoreRpmLimiter.class})
class HistoryRestControllerRateLimitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetHistoryUseCase getHistoryUseCase;

    @MockBean
    private AsyncLogPublisher asyncLogPublisher;

    @Test
    @DisplayName("Debe devolver HTTP 429 con lista vacía al superar 3 solicitudes en /api/history")
    void shouldReturn429WithEmptyListOnHistoryRateLimit() throws Exception {
        when(getHistoryUseCase.getHistory(anyInt(), anyInt())).thenReturn(Collections.emptyList());

        String endpoint = "/api/history";

        // 3 peticiones OK
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get(endpoint)
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        // 4ta petición -> 429 Too Many Requests
        mockMvc.perform(get(endpoint)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isTooManyRequests());
    }
}