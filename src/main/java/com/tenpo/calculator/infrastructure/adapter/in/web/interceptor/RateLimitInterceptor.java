package com.tenpo.calculator.infrastructure.adapter.in.web.interceptor;

import com.tenpo.calculator.domain.limiters.RpmLimiter;
import com.tenpo.calculator.infrastructure.adapter.in.web.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.server.ResponseStatusException;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RpmLimiter rpmLimiter;

    public RateLimitInterceptor(RpmLimiter rpmLimiter) {
        this.rpmLimiter = rpmLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Obtenemos la IP del cliente (considerando si pasa por proxies/load balancers)
        String clientIp = getClientIp(request);

        // Validamos si la IP puede pasar según el límite de 3 RPM
        if (!rpmLimiter.allowRequest(clientIp)) {
            throw new RateLimitExceededException("Rate limit exceeded. Maximum 3 requests per minute allowed.");
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}