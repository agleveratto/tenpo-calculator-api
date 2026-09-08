package com.tenpo.calculator.domain.port.in;

import com.tenpo.calculator.domain.model.ApiLog;
import java.util.List;

public interface GetHistoryUseCase {
    List<ApiLog> getHistory(int page, int size);
}