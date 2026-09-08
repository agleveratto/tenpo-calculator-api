package com.tenpo.calculator.infrastructure.adapter.in.web.interceptor;

import com.tenpo.calculator.infrastructure.adapter.in.web.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateLimitInterceptorTest {

    private RateLimitInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        interceptor = new RateLimitInterceptor();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("Debe permitir las primeras 3 peticiones dentro de la misma ventana de tiempo")
    void shouldAllowFirstThreeRequests() {
        request.setRequestURI("/api/calculate");
        request.setParameter("num1", "10.0");
        request.setParameter("num2", "20.0");

        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
    }

    @Test
    @DisplayName("Debe lanzar RateLimitExceededException en la 4ta petición para /api/calculate conservando path y parámetros")
    void shouldThrowExceptionOnFourthRequestForCalculateEndpoint() {
        request.setRequestURI("/api/calculate");
        request.setParameter("num1", "15.5");
        request.setParameter("num2", "24.5");

        // Consumir las 3 permitidas
        for (int i = 0; i < 3; i++) {
            interceptor.preHandle(request, response, new Object());
        }

        // 4ta petición lanzando excepción
        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessage("Se ha excedido el límite permitido de 3 solicitudes por minuto.")
                .satisfies(ex -> {
                    RateLimitExceededException rateEx = (RateLimitExceededException) ex;
                    assertThat(rateEx.getPath()).isEqualTo("/api/calculate");
                    assertThat(rateEx.getNum1()).isEqualTo(15.5);
                    assertThat(rateEx.getNum2()).isEqualTo(24.5);
                });
    }

    @Test
    @DisplayName("Debe lanzar RateLimitExceededException en la 4ta petición para /api/history capturando el path /api/history")
    void shouldThrowExceptionOnFourthRequestForHistoryEndpoint() {
        request.setRequestURI("/api/history");
        request.setParameter("page", "0");
        request.setParameter("size", "10");

        // Consumir las 3 permitidas
        for (int i = 0; i < 3; i++) {
            interceptor.preHandle(request, response, new Object());
        }

        // 4ta petición lanzando excepción
        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(RateLimitExceededException.class)
                .satisfies(ex -> {
                    RateLimitExceededException rateEx = (RateLimitExceededException) ex;
                    assertThat(rateEx.getPath()).isEqualTo("/api/history");
                    // Al no tener num1 ni num2, devuelven 0.0 por defecto
                    assertThat(rateEx.getNum1()).isEqualTo(0.0);
                    assertThat(rateEx.getNum2()).isEqualTo(0.0);
                });
    }

    @Test
    @DisplayName("Debe retornar 0.0 en los números cuando se reciben parámetros con formato no válido")
    void shouldHandleInvalidFormatParametersOnException() {
        request.setRequestURI("/api/calculate");
        request.setParameter("num1", "invalid_number");
        request.setParameter("num2", "abc");

        for (int i = 0; i < 3; i++) {
            interceptor.preHandle(request, response, new Object());
        }

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(RateLimitExceededException.class)
                .satisfies(ex -> {
                    RateLimitExceededException rateEx = (RateLimitExceededException) ex;
                    assertThat(rateEx.getNum1()).isEqualTo(0.0);
                    assertThat(rateEx.getNum2()).isEqualTo(0.0);
                });
    }

    @Test
    @DisplayName("Debe reiniciar la ventana y permitir peticiones tras transcurrir más de 1 minuto")
    void shouldResetWindowAndAllowRequestWhenMinuteHasPassed() throws Exception {
        request.setRequestURI("/api/calculate");

        for (int i = 0; i < 3; i++) {
            interceptor.preHandle(request, response, new Object());
        }

        // Simulamos por reflección que la ventana inició hace 61 segundos
        Field windowStartField = RateLimitInterceptor.class.getDeclaredField("windowStart");
        windowStartField.setAccessible(true);
        AtomicLong windowStart = (AtomicLong) windowStartField.get(interceptor);
        windowStart.set(System.currentTimeMillis() - 61_000);

        boolean result = interceptor.preHandle(request, response, new Object());
        assertThat(result).isTrue();
    }
}