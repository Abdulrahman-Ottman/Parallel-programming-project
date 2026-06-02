package com.example.concurrencylab.aspect;

import com.example.concurrencylab.service.BenchmarkService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class BenchmarkLoggingAspect {

    @Around("execution(public com.example.concurrencylab.service.BenchmarkService.BenchmarkResult " +
            "com.example.concurrencylab.service.BenchmarkService.runScenario(..))")
    public Object logBenchmark(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        if (result instanceof BenchmarkService.BenchmarkResult r) {
            String scenarioName = (String) joinPoint.getArgs()[2];

            if (!"WARMUP".equalsIgnoreCase(scenarioName)) {
                printResult(r);
            }
        }

        return result;
    }

    private void printResult(BenchmarkService.BenchmarkResult r) {
        System.out.println("\n------------------------------");
        System.out.println("Scenario: " + r.scenario());
        System.out.println("Orders Time: " + r.ordersMs() + " ms");
        System.out.println("Report Time: " + r.reportMs() + " ms");
        System.out.println("------------------------------");
    }
}