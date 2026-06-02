package com.example.concurrencylab.aspect;

import com.example.concurrencylab.service.BenchmarkService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
@Component
public class BenchmarkLoggingAspect {

    private static final String BENCHMARK_LOG_FILE = "benchmark_results.txt";

    @Around("execution(public com.example.concurrencylab.service.BenchmarkService.BenchmarkResult " +
            "com.example.concurrencylab.service.BenchmarkService.runScenario(..))")
    public Object logBenchmark(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        if (result instanceof BenchmarkService.BenchmarkResult r) {
            String scenarioName = (String) joinPoint.getArgs()[2];

            if (!"WARMUP".equalsIgnoreCase(scenarioName)) {
                writeResultToFile(r);
            }
        }

        return result;
    }

    private void writeResultToFile(BenchmarkService.BenchmarkResult r) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(BENCHMARK_LOG_FILE, true))) {
            writer.println("\n------------------------------");
            writer.println("Timestamp: " + getCurrentTimestamp());
            writer.println("Scenario: " + r.scenario());
            writer.println("Orders Time: " + r.ordersMs() + " ms");
            writer.println("Report Time: " + r.reportMs() + " ms");
            writer.println("------------------------------");
            writer.flush();
        } catch (IOException e) {
            System.err.println("Failed to write benchmark results to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}