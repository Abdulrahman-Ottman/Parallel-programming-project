package com.example.concurrencylab.service;

import com.example.concurrencylab.reports.ReportJob;
import com.example.concurrencylab.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BenchmarkService {

    private final ReportJob reportJob;

    public BenchmarkService(ReportJob reportJob) {
        this.reportJob = reportJob;
    }

    public BenchmarkResult runScenario(UserRepository userRepository,
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
                    fail,
                    batching
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
                ordersThread.start();
                reportThread.start();

                ordersThread.join();
                reportThread.join();
            } else {
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

    private long runOrdersAndMeasure(UserRepository userRepository,
                                     ExecutorService executor,
                                     AtomicInteger success,
                                     AtomicInteger fail,boolean useQueue) {


        int numberOfUsers = 3;
        int ordersPerUser = 2;

        List<Future<?>> futures = new ArrayList<>();

        long start = System.nanoTime();

        for (int i = 1; i <= numberOfUsers; i++) {
            final long userId = i;

            for (int j = 0; j < ordersPerUser; j++) {
                futures.add(executor.submit(() -> {
                    try {
                        long productId = ThreadLocalRandom.current().nextLong(1, 11);

                        String endpoint = useQueue ? "/shop/buy" : "/shop/buy/no-queue";

                        String urlStr = "http://localhost:8080" + endpoint +
                                "?userId=" + userId +
                                "&productId=" + productId +
                                "&code=SAVE10" +
                                "&discountValue=10";

                        URL url = new URL(urlStr);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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

    private long toMs(long nano) {
        return TimeUnit.NANOSECONDS.toMillis(nano);
    }

    public record BenchmarkResult(
            String scenario,
            long ordersMs,
            long reportMs,
            long totalMs
    ) {}
}