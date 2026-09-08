package com.tenpo.calculator.infrastructure.adapter.in.web.interceptor;

import com.tenpo.calculator.infrastructure.adapter.in.web.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int MAX_REQUESTS = 3;
    private static final long ONE_MINUTE_IN_MS = 60_000;

    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long now = System.currentTimeMillis();
        long currentWindow = windowStart.get();

        if (now - currentWindow > ONE_MINUTE_IN_MS) {
            if (windowStart.compareAndSet(currentWindow, now)) {
                requestCount.set(0);
            }
        }

        if (requestCount.incrementAndGet() <= MAX_REQUESTS) {
            return true;
        }

        String path = request.getRequestURI();
        double num1 = parseDouble(request.getParameter("num1"));
        double num2 = parseDouble(request.getParameter("num2"));

        throw new RateLimitExceededException(
                "Se ha excedido el límite permitido de 3 solicitudes por minuto.",
                path,
                num1,
                num2
        );
    }

    private double parseDouble(String param) {
        try {
            return param != null ? Double.parseDouble(param) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}