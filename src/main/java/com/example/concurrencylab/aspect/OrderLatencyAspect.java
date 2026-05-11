package com.example.concurrencylab.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OrderLatencyAspect {

    private final OrderLatencyMetrics metrics;

    public OrderLatencyAspect(OrderLatencyMetrics metrics) {
        this.metrics = metrics;
    }

    @Around("@annotation(trackOrderLatency)")
    public Object measure(ProceedingJoinPoint joinPoint,
                          TrackOrderLatency trackOrderLatency) throws Throwable {

        long start = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long end = System.nanoTime();
            metrics.record(trackOrderLatency.scenario(), end - start);
        }
    }
}