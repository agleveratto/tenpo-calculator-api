package com.tenpo.calculator.infrastructure.adapter.in.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class RateLimitInterceptorTest {

    private SemaphoreRpmLimiter rpmLimiter;
    private RateLimitInterceptor interceptor;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        rpmLimiter = Mockito.mock(SemaphoreRpmLimiter.class);
        interceptor = new RateLimitInterceptor(rpmLimiter);

        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);

        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        when(request.getRemoteAddr()).thenReturn("192.168.1.50");
        when(request.getRequestURI()).thenReturn("/api/history");
    }

    @Test
    void shouldAllowRequestWhenLimitNotExceeded() throws Exception {
        when(rpmLimiter.allowRequest(eq("192.168.1.50"), eq("/api/history"))).thenReturn(true);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        Mockito.verify(response, Mockito.never()).setStatus(429);
    }

    @Test
    void shouldBlockRequestAndReturn429WhenLimitExceeded() throws Exception {
        when(rpmLimiter.allowRequest(eq("192.168.1.50"), eq("/api/history"))).thenReturn(false);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        Mockito.verify(response).setStatus(429);
        Mockito.verify(response).setContentType("text/plain;charset=UTF-8");
        assertTrue(responseWriter.toString().contains("Rate limit exceeded"));
    }
}