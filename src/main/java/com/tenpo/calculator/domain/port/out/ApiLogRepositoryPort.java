package com.tenpo.calculator.domain.port.out;

import com.tenpo.calculator.domain.model.ApiLog;
import java.util.List;

public interface ApiLogRepositoryPort {
    void saveLog(ApiLog entry);
    List<ApiLog> findAll(int page, int size);
}