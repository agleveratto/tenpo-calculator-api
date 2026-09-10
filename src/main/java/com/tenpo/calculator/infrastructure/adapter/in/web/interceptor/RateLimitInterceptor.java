package com.tenpo.calculator.infrastructure.adapter.in.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final SemaphoreRpmLimiter rpmLimiter;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = request.getRemoteAddr();
        String uri = request.getRequestURI();

        // Validamos pasándole tanto la IP como la URI para que tengan contadores independientes
        if (!rpmLimiter.allowRequest(clientIp, uri)) {
            response.setStatus(429);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Rate limit exceeded. Maximum 3 requests per minute allowed for " + uri);
            return false;
        }

        return true;
    }
}