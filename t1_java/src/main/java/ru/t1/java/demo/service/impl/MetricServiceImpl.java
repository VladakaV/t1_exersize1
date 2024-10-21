package ru.t1.java.demo.service.impl;

import com.google.common.collect.ImmutableList;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.service.MetricService;
import ru.t1.java.demo.service.mterics.MetricCounter;

import java.util.HashMap;
import java.util.Map;

import static ru.t1.java.demo.model.enums.Metrics.CLIENT_CONTROLLER_REQUEST_COUNT;

@Component
public class MetricServiceImpl implements MetricService {

    private final Map<String, MetricCounter> counters;
    private final MeterRegistry meterRegistry;
    private final Tag groupTag = new ImmutableTag("group", "t1_java");

    public MetricServiceImpl(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.counters = new HashMap<>();
        counters.put(CLIENT_CONTROLLER_REQUEST_COUNT.getValue(),
                counter(CLIENT_CONTROLLER_REQUEST_COUNT.getValue()));
    }

    @Override
    public void incrementByName(String name) {
        counters.get(CLIENT_CONTROLLER_REQUEST_COUNT.getValue()).increment();
    }

    private MetricCounter counter(String name) {
        return MetricCounter.create(meterRegistry, name, ImmutableList.of(groupTag), b -> {
        });
    }
}
