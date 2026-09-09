package com.tenpo.calculator.infrastructure.adapter.in.web.interceptor;

import com.tenpo.calculator.domain.limiters.RpmLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class SemaphoreRpmLimiter implements RpmLimiter {
    private final ConcurrentHashMap<String, AtomicInteger> ipRequestCount = new ConcurrentHashMap<>();
    private long startTime = System.currentTimeMillis();

    @Override
    public boolean allowRequest(String ipAddress) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;

        if (elapsedTime >= 60000) { // Ventana de 60 segundos
            startTime = currentTime;
            ipRequestCount.clear();
        }

        AtomicInteger requestCount = ipRequestCount.computeIfAbsent(ipAddress, k -> new AtomicInteger(0));

        int currentCount = requestCount.incrementAndGet();
        return currentCount <= 3; // Máximo 3 RPM requerido por Tenpo
    }
}