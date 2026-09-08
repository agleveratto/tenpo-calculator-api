package com.tenpo.calculator.infrastructure.adapter.out.persistence.web;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaLogRepository extends JpaRepository<ApiLogJpaEntity, Long> {}