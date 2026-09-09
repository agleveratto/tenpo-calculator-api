package com.tenpo.calculator.domain.limiters;

public interface RpmLimiter {
     boolean allowRequest(String ipAddress);
}
