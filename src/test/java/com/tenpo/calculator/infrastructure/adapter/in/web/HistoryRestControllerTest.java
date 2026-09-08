package com.tenpo.calculator.infrastructure.adapter.in.web;

import com.tenpo.calculator.domain.model.ApiLog;
import com.tenpo.calculator.domain.port.in.GetHistoryUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(HistoryRestController.class)
class HistoryRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetHistoryUseCase getHistoryUseCase;

    @Test
    void shouldReturn200AndEmptyHistory() throws Exception {
        List<ApiLog> emptyList = List.of();
        when(getHistoryUseCase.getHistory(0,10)).thenReturn(emptyList);

        String endpoint = "/api/history";
        mockMvc.perform(get(endpoint)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty())
                .andReturn();

        verify(getHistoryUseCase).getHistory(0,10);
    }

    @Test
    void shouldReturn200AndApiLogHistory() throws Exception {
        String endpoint = "/api/history";
        ApiLog mockLog = new ApiLog(1L, LocalDateTime.now(), "/api/calculate", "num1=5, num2=5", "11.0", null);
        List<ApiLog> emptyList = List.of(mockLog);
        when(getHistoryUseCase.getHistory(0,10)).thenReturn(emptyList);

        mockMvc.perform(get(endpoint)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$[0].endpoint").value("/api/calculate"))
                .andExpect(jsonPath("$[0].parameters").value("num1=5, num2=5"))
                .andExpect(jsonPath("$[0].response").value("11.0"))
                .andExpect(jsonPath("$[0].error").isEmpty())
                .andReturn();

        verify(getHistoryUseCase).getHistory(0,10);
    }
}