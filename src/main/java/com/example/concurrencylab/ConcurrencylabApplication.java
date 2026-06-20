package com.example.concurrencylab;

import com.example.concurrencylab.aspect.OrderLatencyMetrics;
import com.example.concurrencylab.repository.UserRepository;
import com.example.concurrencylab.service.BenchmarkService;
import com.example.concurrencylab.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
@SpringBootApplication
@EnableCaching
public class ConcurrencylabApplication {

    private final BenchmarkService benchmarkService;
    @Autowired
    private final ProductService productService ;
    public ConcurrencylabApplication(BenchmarkService benchmarkService , ProductService productService) {
        this.benchmarkService = benchmarkService;
        this.productService = productService;
    }

    public static void main(String[] args) {
//        SpringApplication.run(ConcurrencylabApplication.class, args);
        ConfigurableApplicationContext context =
                SpringApplication.run(ConcurrencylabApplication.class, args);

        context.close();

    }

    @Bean
    public CommandLineRunner run(UserRepository userRepository, OrderLatencyMetrics metrics , ProductService productService
    ) {
        return args -> {
//            benchmarkService.runScenario(userRepository, true, "WARMUP");
//            benchmarkService.runScenario(userRepository, false, "WITHOUT BATCHING");
//            benchmarkService.runScenario(userRepository, true, "WITH BATCHING");
//           long id = productService.preloadMostSoldProduct();
//
//            benchmarkService.benchmarkProductCache(id);
//            metrics.printSummary();




            benchmarkService.runTransactionalTest();

        };
    }

}