package com.tenpo.calculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(info = @Info(title = "Tenpo Calculator API",
        description = "Challenge Backend - API de cálculo con porcentaje, historial y rate limiting.",
        version = "1.0.0"))
@EnableAsync
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.tenpo.calculator.infrastructure.adapter.out.persistence")
public class CalculatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CalculatorApplication.class, args);
    }
}