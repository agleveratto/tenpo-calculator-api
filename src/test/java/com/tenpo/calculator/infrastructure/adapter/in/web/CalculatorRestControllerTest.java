package com.tenpo.calculator.infrastructure.adapter.in.web;

import com.tenpo.calculator.domain.model.CalculationResult;
import com.tenpo.calculator.domain.port.in.CalculateUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CalculatorRestController.class)
class CalculatorRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalculateUseCase calculateUseCase;

    @Test
    @DisplayName("GET /api/calculate debe retornar 200 OK con el record del resultado")
    void shouldReturn200AndCalculationResult() throws Exception {
        CalculationResult mockResult = new CalculationResult(5.0, 5.0, 10.0, 11.0);

        when(calculateUseCase.calculate(5.0, 5.0)).thenReturn(mockResult);

        mockMvc.perform(get("/api/calculate")
                        .param("num1", "5.0")
                        .param("num2", "5.0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.num1").value(5.0))
                .andExpect(jsonPath("$.num2").value(5.0))
                .andExpect(jsonPath("$.percentageApplied").value(10.0))
                .andExpect(jsonPath("$.finalResult").value(11.0));

        verify(calculateUseCase).calculate(5.0, 5.0);
    }
}