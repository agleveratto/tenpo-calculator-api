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

    // Clave compuesta: "IP:URI" para aislar los contadores por endpoint
    private final ConcurrentHashMap<String, AtomicInteger> requestCountMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> startTimeMap = new ConcurrentHashMap<>();

    private final Supplier<Long> timeProvider;

    public SemaphoreRpmLimiter() {
        this(System::currentTimeMillis);
    }

    public SemaphoreRpmLimiter(Supplier<Long> timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * Valida el límite de peticiones por minuto (RPM) por IP y por Endpoint específico.
     */
    public boolean allowRequest(String ipAddress, String endpoint) {
        long currentTime = timeProvider.get();
        String cacheKey = ipAddress + ":" + endpoint;

        long startTime = startTimeMap.computeIfAbsent(cacheKey, k -> currentTime);
        long elapsedTime = currentTime - startTime;

        // Si pasó un minuto (60000 ms), reiniciamos la ventana de tiempo y el contador
        if (elapsedTime >= 60000) {
            startTimeMap.put(cacheKey, currentTime);
            AtomicInteger count = requestCountMap.get(cacheKey);
            if (count != null) {
                count.set(0);
            }
        }

        AtomicInteger requestCount = requestCountMap.computeIfAbsent(cacheKey, k -> new AtomicInteger(0));
        int currentCount = requestCount.incrementAndGet();

        return currentCount <= 3; // Máximo 3 RPM por endpoint/IP
    }

    @Override
    public boolean allowRequest(String ipAddress) {
        // Fallback por si se invoca la interfaz genérica sin especificar endpoint
        return allowRequest(ipAddress, "default");
    }
}