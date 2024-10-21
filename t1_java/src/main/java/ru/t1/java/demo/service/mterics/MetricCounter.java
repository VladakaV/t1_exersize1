package ru.t1.java.demo.service.mterics;

import com.google.common.collect.ImmutableList;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

import javax.annotation.concurrent.ThreadSafe;
import java.util.function.Consumer;

@ThreadSafe
public class MetricCounter {

    private final Counter counter;

    private MetricCounter(final Counter counter) {
        this.counter = counter;
    }

    public static MetricCounter create(
            final MeterRegistry meterRegistry,
            final String name,
            final ImmutableList<Tag> tags,
            final Consumer<Counter.Builder> builder) {
        final var meterCounter = Counter.builder(name);
        meterCounter.tags(tags);
        builder.accept(meterCounter);
        return new MetricCounter(meterCounter.register(meterRegistry));
    }

    /**
     * @return The cumulative count since this counter was created.
     */
    public double count() {
        return this.counter.count();
    }

    public void increment() {
        this.counter.increment();
    }

    public void increment(final double amount) {
        this.counter.increment(amount);
    }
}