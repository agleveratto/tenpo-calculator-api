package com.tenpo.calculator.infrastructure.adapter.out.external;

import com.tenpo.calculator.domain.port.out.DynamicPercentagePort;
import org.springframework.stereotype.Component;

@Component
public class PercentageMockAdapter implements DynamicPercentagePort {

    @Override
    public double getPercentage() {
        return 10.0;
    }
}