package com.example.concurrencylab;

import com.example.concurrencylab.reports.ReportJob;
import com.example.concurrencylab.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class ConcurrencylabApplication {

    private final ReportJob reportJob;

    public ConcurrencylabApplication(ReportJob reportJob) {
        this.reportJob = reportJob;
    }

    public static void main(String[] args) {
        SpringApplication.run(ConcurrencylabApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(UserRepository userRepository) {
        return args -> {

            // warm-up (ignored)
            runScenario(userRepository, true, "WARMUP");

            BenchmarkResult noBatch =
                    runScenario(userRepository, false, "WITHOUT BATCHING");

            BenchmarkResult batch =
                    runScenario(userRepository, true, "WITH BATCHING");

            printResult(noBatch);
            printResult(batch);
        };
    }

    // =========================
    // Benchmark Runner
    // =========================
    private BenchmarkResult runScenario(UserRepository userRepository,
                                        boolean batching,
                                        String name) {

        ExecutorService executor = Executors.newFixedThreadPool(24);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        long totalStart = System.nanoTime();

        final long[] ordersTime = new long[1];
        final long[] reportTime = new long[1];

        Thread ordersThread = new Thread(() -> {
            ordersTime[0] = runOrdersAndMeasure(
                    userRepository,
                    executor,
                    success,
                    fail
            );
        });

        Thread reportThread = new Thread(() -> {
            long start = System.nanoTime();

            if (batching) {
                reportJob.runReportWithBatching(executor);
            } else {
                reportJob.runReportWithoutBatching(executor);
            }

            long end = System.nanoTime();
            reportTime[0] = toMs(end - start);
        });

        try {
            if (!batching) {
                // Non-batching: orders and reports use the same 24-thread pool at the same time
                ordersThread.start();
                reportThread.start();

                ordersThread.join();
                reportThread.join();
            } else {
                // Batching: orders finish first, then reports start, but still use the same 24-thread pool
                ordersThread.start();
                ordersThread.join();

                reportThread.start();
                reportThread.join();
            }

            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.MINUTES);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        long totalEnd = System.nanoTime();

        return new BenchmarkResult(
                name,
                ordersTime[0],
                reportTime[0],
                toMs(totalEnd - totalStart)
        );
    }

    // =========================
    // Correct Orders Measurement
    // =========================
    private long runOrdersAndMeasure(UserRepository userRepository,
                                     ExecutorService executor,
                                     AtomicInteger success,
                                     AtomicInteger fail) {

        int numberOfUsers = 3;
        int ordersPerUser = 2;

        List<Future<?>> futures = new ArrayList<>();

        long start = System.nanoTime();

        for (int i = 1; i <= numberOfUsers; i++) {

            final long userId = i;

            for (int j = 0; j < ordersPerUser; j++) {

                futures.add(executor.submit(() -> {
                    try {

                        long productId =
                                ThreadLocalRandom.current().nextLong(1, 11);

                        String urlStr = "http://localhost:8080/shop/buy" +
                                "?userId=" + userId +
                                "&productId=" + productId +
                                "&code=SAVE10" +
                                "&discountValue=10";

                        URL url = new URL(urlStr);

                        HttpURLConnection conn =
                                (HttpURLConnection) url.openConnection();

                        conn.setRequestMethod("POST");

                        int responseCode = conn.getResponseCode();

                        if (responseCode == 200) {
                            success.incrementAndGet();
                        } else {
                            fail.incrementAndGet();
                        }

                        conn.disconnect();

                    } catch (Exception e) {
                        fail.incrementAndGet();
                    }
                }));
            }
        }

        // WAIT for all orders to finish
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        long end = System.nanoTime();
        return toMs(end - start);
    }

    private void waitForExecutor(ExecutorService executor) {
        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private long toMs(long nano) {
        return TimeUnit.NANOSECONDS.toMillis(nano);
    }

    // =========================
    // Result
    // =========================
    record BenchmarkResult(
            String scenario,
            long ordersMs,
            long reportMs,
            long totalMs
    ) {}

    private void printResult(BenchmarkResult r) {
        System.out.println("\n------------------------------");
        System.out.println("Scenario: " + r.scenario());
        System.out.println("Orders Time: " + r.ordersMs() + " ms");
        System.out.println("Report Time: " + r.reportMs() + " ms");
//        System.out.println("Total Time: " + r.totalMs() + " ms");
        System.out.println("------------------------------");
    }
}