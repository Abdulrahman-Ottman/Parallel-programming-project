package com.example.concurrencylab;

import com.example.concurrencylab.aspect.OrderLatencyMetrics;
import com.example.concurrencylab.repository.UserRepository;
import com.example.concurrencylab.service.BenchmarkService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ConcurrencylabApplication {

    private final BenchmarkService benchmarkService;

    public ConcurrencylabApplication(BenchmarkService benchmarkService) {
        this.benchmarkService = benchmarkService;
    }

    public static void main(String[] args) {
        SpringApplication.run(ConcurrencylabApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(UserRepository userRepository, OrderLatencyMetrics metrics) {
        return args -> {
//            benchmarkService.runScenario(userRepository, true, "WARMUP");
//            benchmarkService.runScenario(userRepository, false, "WITHOUT BATCHING");
//            benchmarkService.runScenario(userRepository, true, "WITH BATCHING");
//            metrics.printSummary();


            benchmarkService.runTransactionalTest();
        };
    }
}