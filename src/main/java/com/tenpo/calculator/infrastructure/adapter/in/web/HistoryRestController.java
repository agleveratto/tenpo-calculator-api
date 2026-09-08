package com.tenpo.calculator.infrastructure.adapter.in.web;

import com.tenpo.calculator.domain.model.ApiLog;
import com.tenpo.calculator.domain.port.in.GetHistoryUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class HistoryRestController {

    private final GetHistoryUseCase getHistoryUseCase;

    public HistoryRestController(GetHistoryUseCase getHistoryUseCase) {
        this.getHistoryUseCase = getHistoryUseCase;
    }

    @GetMapping("/history")
    public ResponseEntity<List<ApiLog>> getHistory(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(getHistoryUseCase.getHistory(page, size));
    }
}
