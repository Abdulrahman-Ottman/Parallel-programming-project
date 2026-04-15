package com.example.concurrencylab;

import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;

@RestController
@RequestMapping("/discount")
public class DiscountController {

    private DiscountService service = new DiscountService();
    @GetMapping("/status")
    public Object status() {
        return service.getUsers();
    }
    @GetMapping("/count")
    public int count() {
        return service.getCurrentUses();
    }

    @PostMapping("/apply")
    public String apply(@RequestParam String user) {
        return service.applyDiscount(user);
    }

    @PostMapping("/simulate")
    public String simulate() {

        ExecutorService executor = Executors.newFixedThreadPool(20);

        for (int i = 1; i <= 50; i++) {
            String userId = "user" + i;

            executor.submit(() -> {
                try {
                    URL url = new URL("http://localhost:8080/discount/apply?user=" + userId);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setRequestMethod("POST");
                    conn.setConnectTimeout(2000);
                    conn.setReadTimeout(2000);

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