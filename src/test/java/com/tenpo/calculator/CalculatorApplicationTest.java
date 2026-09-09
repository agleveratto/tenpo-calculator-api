package com.tenpo.calculator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CalculatorApplicationTest {

    @Test
    void shouldLoadContextAndRunMain() {
        CalculatorApplication.main(new String[]{});
    }
}