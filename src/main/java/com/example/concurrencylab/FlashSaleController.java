package com.example.concurrencylab;

import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/flash")
public class FlashSaleController {
    private FlashSaleService service = new FlashSaleService();

    @GetMapping("/stock")
    public int getStock() {
        return service.getStock();
    }

    @GetMapping("/reservations")
    public Object getReservations() {
        return service.getReservations();
    }

    @PostMapping("/reserve")
    public String reserve(@RequestParam String user) {
        return service.reserve(user);
    }

    @PostMapping("/simulate")
    public String simulate() {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 1; i <= 50; i++) {
            String userId = "user" + i;
            executor.submit(() -> {
                String result = service.reserve(userId);
                System.out.println(userId + ": " + result);
            });
        }

        executor.shutdown();
        return "Simulation started!";
    }
}