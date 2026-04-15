package com.example.concurrencylab;

import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.HttpURLConnection;
import java.net.URL;

@RestController
@RequestMapping("/shop")
public class ShopController {

    private OrderService orderService = new OrderService();

    @GetMapping("/orders")
    public Object getOrders() {
        return orderService.getOrders();
    }

    @GetMapping("/discount/count")
    public int count() {
        return orderService.getDiscountService().getCurrentUses();
    }

    @PostMapping("/buy")
    public String buy(
            @RequestParam String user,
            @RequestParam String product,
            @RequestParam(required = false) String code
    ) {
        return orderService.buy(user, product, code);
    }

    @PostMapping("/simulate")
    public String simulate() {

        ExecutorService executor = Executors.newFixedThreadPool(10);
//        ExecutorService executor = Executors.newCachedThreadPool();

        for (int i = 1; i <= 100; i++) {
            String userId = "user" + i;
            String productId = "product1";

            executor.submit(() -> {
                try {
                    URL url = new URL(
                            "http://localhost:8080/shop/buy?user="
                                    + userId +
                                    "&product=" + productId +
                                    "&code=SAVE10"
                    );

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");

                    int responseCode = conn.getResponseCode();

                    System.out.println(userId + " -> " + responseCode);

                    conn.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();

        return "Simulation started";
    }
}