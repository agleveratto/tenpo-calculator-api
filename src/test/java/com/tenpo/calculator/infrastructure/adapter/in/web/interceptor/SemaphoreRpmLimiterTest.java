package com.tenpo.calculator.infrastructure.adapter.in.web.interceptor;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class SemaphoreRpmLimiterTest {

    @Test
    void shouldAllowRequestsUpToLimit() {
        SemaphoreRpmLimiter rpmLimiter = new SemaphoreRpmLimiter();
        String ip = "192.168.1.1";

        assertTrue(rpmLimiter.allowRequest(ip));
        assertTrue(rpmLimiter.allowRequest(ip));
        assertTrue(rpmLimiter.allowRequest(ip));
    }

    @Test
    void shouldBlockRequestWhenLimitIsExceeded() {
        SemaphoreRpmLimiter rpmLimiter = new SemaphoreRpmLimiter();
        String ip = "192.168.1.2";

        assertTrue(rpmLimiter.allowRequest(ip));
        assertTrue(rpmLimiter.allowRequest(ip));
        assertTrue(rpmLimiter.allowRequest(ip));

        assertFalse(rpmLimiter.allowRequest(ip));
    }

    @Test
    void shouldTrackDifferentIpsIndependently() {
        SemaphoreRpmLimiter rpmLimiter = new SemaphoreRpmLimiter();
        String ip1 = "192.168.1.10";
        String ip2 = "192.168.1.20";

        assertTrue(rpmLimiter.allowRequest(ip1));
        assertTrue(rpmLimiter.allowRequest(ip1));
        assertTrue(rpmLimiter.allowRequest(ip1));
        assertFalse(rpmLimiter.allowRequest(ip1));

        assertTrue(rpmLimiter.allowRequest(ip2));
        assertTrue(rpmLimiter.allowRequest(ip2));
    }

    @Test
    void shouldResetCountWhenTimeWindowExpires() {
        // Creamos un reloj simulado usando un AtomicLong
        AtomicLong mockTime = new AtomicLong(1000000L);
        SemaphoreRpmLimiter rpmLimiter = new SemaphoreRpmLimiter(mockTime::get);

        String ip = "192.168.1.3";

        // Agota el límite en el tiempo inicial
        assertTrue(rpmLimiter.allowRequest(ip));
        assertTrue(rpmLimiter.allowRequest(ip));
        assertTrue(rpmLimiter.allowRequest(ip));
        assertFalse(rpmLimiter.allowRequest(ip)); // Bloqueado

        // Avanzamos el tiempo simulado en más de 60 segundos (65000 ms)
        mockTime.addAndGet(65000L);

        // Debería permitir solicitudes nuevamente porque la ventana expiró
        assertTrue(rpmLimiter.allowRequest(ip));
    }
}