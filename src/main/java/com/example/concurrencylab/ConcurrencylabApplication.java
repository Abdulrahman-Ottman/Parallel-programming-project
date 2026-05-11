package com.example.concurrencylab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class ConcurrencylabApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(ConcurrencylabApplication.class, args);
        Thread.sleep(3000);
        simulate();

	}
    public static String simulate() {

        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        //N * (1 + W/C) بما التعامل مع المحافظ و الداتا بيز تعتبر عمليات IO ممكن تاخد وقت كبير
        // 8 * (1 + 100/50) = 24
        // على فرض وقت التنفيذ هو 50 ميلي ثانية

        ExecutorService executor = Executors.newFixedThreadPool(24);
//        ExecutorService executor = Executors.newCachedThreadPool();

        for (int i = 1; i <= 10; i++) {
            String userId = "user" + i;

            executor.submit(() -> {
                try {


//                 //buyWithQueue
                    URL url = new URL("http://localhost:8080/shop/buy?user=" + userId + "&product=product1&code=SAVE10");
                    //buyWithOutQueue


//                    URL url = new URL("http://localhost:8080/shop/buy/no-queue?user=" + userId + "&product=product1&code=SAVE10");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");

                    int responseCode = conn.getResponseCode();

                    if (responseCode == 200) {
                        success.getAndIncrement();
                    } else {
                        fail.getAndIncrement();
                    }

                    conn.disconnect();

                } catch (Exception e) {
                    fail.incrementAndGet();
                }
            });
        }


        
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        }catch (Exception e){
            Thread.currentThread().interrupt();
        }



        System.out.println("Success: " + success.get());
        System.out.println("Fail: " + fail.get());

        return "simulate started";
    }
}
