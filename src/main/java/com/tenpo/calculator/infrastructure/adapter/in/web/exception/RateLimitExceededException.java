package com.tenpo.calculator.infrastructure.adapter.in.web.exception;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends RuntimeException {
    private final String path;
    private final double num1;
    private final double num2;

    public RateLimitExceededException(String message, String path, double num1, double num2) {
        super(message);
        this.path = path;
        this.num1 = num1;
        this.num2 = num2;
    }
}