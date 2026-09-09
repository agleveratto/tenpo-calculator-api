package com.tenpo.calculator.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataJpaLogRepository extends JpaRepository<ApiLogJpaEntity, Long> {}