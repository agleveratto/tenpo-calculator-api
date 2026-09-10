package com.tenpo.calculator.infrastructure.adapter.in.web.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SemaphoreRpmLimiterTest {

    private SemaphoreRpmLimiter rpmLimiter;
    private AtomicLong fakeTime;

    @BeforeEach
    void setUp() {
        fakeTime = new AtomicLong(1000L);
        // Inyectamos un timeProvider controlado para tests deterministas
        rpmLimiter = new SemaphoreRpmLimiter(fakeTime::get);
    }

    @Test
    void shouldAllowUpToThreeRequestsPerEndpointAndBlockFourth() {
        String ip = "127.0.0.1";
        String endpoint = "/api/calculate";

        // 1, 2, 3 deberían pasar
        assertTrue(rpmLimiter.allowRequest(ip, endpoint));
        assertTrue(rpmLimiter.allowRequest(ip, endpoint));
        assertTrue(rpmLimiter.allowRequest(ip, endpoint));

        // La 4ta debe ser bloqueada (excede 3 RPM)
        assertFalse(rpmLimiter.allowRequest(ip, endpoint));
    }

    @Test
    void shouldIsolateCountersBetweenDifferentEndpoints() {
        String ip = "127.0.0.1";
        String calculateEndpoint = "/api/calculate";
        String historyEndpoint = "/api/history";

        // Consumimos el límite de /api/calculate (3 peticiones)
        assertTrue(rpmLimiter.allowRequest(ip, calculateEndpoint));
        assertTrue(rpmLimiter.allowRequest(ip, calculateEndpoint));
        assertTrue(rpmLimiter.allowRequest(ip, calculateEndpoint));
        assertFalse(rpmLimiter.allowRequest(ip, calculateEndpoint)); // Bloqueado

        // /api/history debe seguir libre con sus propias 3 peticiones disponibles
        assertTrue(rpmLimiter.allowRequest(ip, historyEndpoint));
        assertTrue(rpmLimiter.allowRequest(ip, historyEndpoint));
        assertTrue(rpmLimiter.allowRequest(ip, historyEndpoint));
        assertFalse(rpmLimiter.allowRequest(ip, historyEndpoint)); // Bloqueado al 4to
    }

    @Test
    void shouldResetCountAfterOneMinute() {
        String ip = "127.0.0.1";
        String endpoint = "/api/calculate";

        // Agotamos el límite
        assertTrue(rpmLimiter.allowRequest(ip, endpoint));
        assertTrue(rpmLimiter.allowRequest(ip, endpoint));
        assertTrue(rpmLimiter.allowRequest(ip, endpoint));
        assertFalse(rpmLimiter.allowRequest(ip, endpoint));

        // Simulamos que pasan 60 segundos (60000 ms)
        fakeTime.addAndGet(60000L);

        // Debería permitir nuevamente
        assertTrue(rpmLimiter.allowRequest(ip, endpoint));
    }
}