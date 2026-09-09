package com.tenpo.calculator.infrastructure.adapter.in.web.interceptor;

import com.tenpo.calculator.domain.limiters.RpmLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Slf4j
@Component
public class SemaphoreRpmLimiter implements RpmLimiter {
    private final ConcurrentHashMap<String, AtomicInteger> ipRequestCount = new ConcurrentHashMap<>();
    private final Supplier<Long> timeProvider;
    private long startTime;

    // Constructor por defecto para Spring (producción)
    public SemaphoreRpmLimiter() {
        this(System::currentTimeMillis);
    }

    // Constructor para tests (permite controlar el tiempo)
    public SemaphoreRpmLimiter(Supplier<Long> timeProvider) {
        this.timeProvider = timeProvider;
        this.startTime = timeProvider.get();
    }

    @Override
    public boolean allowRequest(String ipAddress) {
        long currentTime = timeProvider.get();
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