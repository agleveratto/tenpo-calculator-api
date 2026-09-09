package com.tenpo.calculator.infrastructure.adapter.in.web.interceptor;

import com.tenpo.calculator.domain.limiters.RpmLimiter;
import com.tenpo.calculator.infrastructure.adapter.in.web.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitInterceptorTest {

    @Mock
    private RpmLimiter rpmLimiter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private RateLimitInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new RateLimitInterceptor(rpmLimiter);
    }

    @Test
    void shouldAllowRequestWhenUnderLimitAndNoForwardedHeader() throws Exception {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("remote-address");
        when(rpmLimiter.allowRequest("remote-address")).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, new Object());

        // Then
        assertTrue(result);
        verify(rpmLimiter).allowRequest("remote-address");
    }

    @Test
    void shouldAllowRequestWhenUnderLimitAndEmptyForwardedHeader() throws Exception {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("fallback-address");
        when(rpmLimiter.allowRequest("fallback-address")).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, new Object());

        // Then
        assertTrue(result);
        verify(rpmLimiter).allowRequest("fallback-address");
    }

    @Test
    void shouldAllowRequestWhenUnderLimitAndHasForwardedHeader() throws Exception {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn("primary-client, proxy-server");
        when(rpmLimiter.allowRequest("primary-client")).thenReturn(true);

        // When
        boolean result = interceptor.preHandle(request, response, new Object());

        // Then
        assertTrue(result);
        verify(rpmLimiter).allowRequest("primary-client");
    }

    @Test
    void shouldThrowRateLimitExceededExceptionWhenLimitIsExceeded() {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("blocked-client");
        when(rpmLimiter.allowRequest("blocked-client")).thenReturn(false);

        // When & Then
        RateLimitExceededException exception = assertThrows(
                RateLimitExceededException.class,
                () -> interceptor.preHandle(request, response, new Object())
        );

        assertEquals("Rate limit exceeded. Maximum 3 requests per minute allowed.", exception.getMessage());
        verify(rpmLimiter).allowRequest("blocked-client");
    }
}