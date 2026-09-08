package com.tenpo.calculator.infrastructure.adapter.out.persistence.web;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_call_logs")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime timestamp;
    private String endpoint;
    private String parameters;
    private String response;
    private String error;
}