package com.example.concurrencylab.aspect;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@Component
public class CacheMetricsWriter {

    private final OrderLatencyMetrics metrics;

    public CacheMetricsWriter(
            OrderLatencyMetrics metrics
    ) {
        this.metrics = metrics;
    }

    @PreDestroy
    public void exportResults() {
        System.out.println(
                "WRITING REPORT ..."
        );
        System.out.println(
                metrics.getAllResults()
        );
        try (PrintWriter writer =
                     new PrintWriter(
                             new FileWriter(
                                     "cache-report.txt"
                             ))) {

            for (String scenario :
                    metrics.getAllResults().keySet()) {

                writer.println(
                        "Scenario: " + scenario
                );

                writer.println(
                        "Calls: " +
                                metrics.getCount(scenario)
                );

                writer.printf(
                        "Average Latency: %.4f ms%n",
                        metrics.averageMs(scenario)
                );

                writer.println(
                        "--------------------"
                );
            }


        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}