package com.tenpo.calculator.infrastructure.adapter.in.web.interceptor;

import com.tenpo.calculator.domain.limiters.RpmLimiter;
import com.tenpo.calculator.infrastructure.adapter.in.web.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateLimitInterceptorTest {

    private RateLimitInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private RpmLimiter rpmLimiter;

    @BeforeEach
    void setUp() {
        // Usamos una implementación real o un mock limpio del limiter
        rpmLimiter = new SemaphoreRpmLimiter();
        interceptor = new RateLimitInterceptor(rpmLimiter);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("Debe permitir las primeras 3 peticiones y bloquear la 4ta con RateLimitExceededException")
    void shouldAllowFirstThreeRequestsAndBlockFourth() {
        request.setRequestURI("/api/calculate");

        // Primeras 3 permitidas
        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();

        // 4ta petición debe lanzar la excepción de límite excedido
        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessage("Rate limit exceeded. Maximum 3 requests per minute allowed.");
    }

    @Test
    @DisplayName("Debe diferenciar límites por IP diferente")
    void shouldAllowDifferentIpsIndependently() {
        request.setRequestURI("/api/calculate");

        // IP 1 consume sus 3 permitidas
        request.setRemoteAddr("192.168.1.10");
        interceptor.preHandle(request, response, new Object());
        interceptor.preHandle(request, response, new Object());
        interceptor.preHandle(request, response, new Object());

        // IP 2 debería poder hacer peticiones aunque IP 1 haya alcanzado su límite
        MockHttpServletRequest requestIp2 = new MockHttpServletRequest();
        requestIp2.setRemoteAddr("192.168.1.20");
        requestIp2.setRequestURI("/api/calculate");

        assertThat(interceptor.preHandle(requestIp2, response, new Object())).isTrue();
    }
}