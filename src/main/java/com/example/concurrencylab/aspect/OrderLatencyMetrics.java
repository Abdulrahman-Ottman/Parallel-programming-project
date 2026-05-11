package com.example.concurrencylab.aspect;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

@Component
public class OrderLatencyMetrics {

    private final Map<String, LongAdder> totalNanos = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> counts = new ConcurrentHashMap<>();

    public void record(String scenario, long nanos) {
        totalNanos.computeIfAbsent(scenario, k -> new LongAdder()).add(nanos);
        counts.computeIfAbsent(scenario, k -> new LongAdder()).increment();
    }

    public double averageMs(String scenario) {
        long count = counts.getOrDefault(scenario, new LongAdder()).sum();
        if (count == 0) return 0;

        long total = totalNanos.getOrDefault(scenario, new LongAdder()).sum();
        return total / count / 1_000_000;
    }

    public void printSummary() {
        System.out.println("\n==============================");
        System.out.println("Scenario: WITH_QUEUE");
        System.out.println("Avg Request-Response Time: " + averageMs("WITH_QUEUE") + " ms");
        System.out.println("------------------------------");
        System.out.println("Scenario: WITHOUT_QUEUE");
        System.out.printf("Avg Request-Response Time: %.2f ms%n", averageMs("WITHOUT_QUEUE"));
        System.out.println("==============================");
    }
}