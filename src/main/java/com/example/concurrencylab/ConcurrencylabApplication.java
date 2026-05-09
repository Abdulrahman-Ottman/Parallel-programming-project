package com.example.concurrencylab;

import com.example.concurrencylab.model.User;
import com.example.concurrencylab.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class ConcurrencylabApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConcurrencylabApplication.class, args);
	}

//    @Bean
//    public CommandLineRunner run(UserRepository userRepository) {
//        return args -> {
//            simulate(userRepository, false); // 👈 first run
//        };
//    }
    public void simulate(UserRepository userRepository,boolean initUsers) {

        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        int numberOfUsers = 200;
        int ordersPerUser = 5;

        // ✅ Step 1: Create users (ONLY FIRST RUN)
        if (initUsers) {
            List<User> users = new ArrayList<>();

            for (int i = 1; i <= numberOfUsers; i++) {
                User user = new User();
                user.setName("User_" + i);
                users.add(user);
            }

            userRepository.saveAll(users);
            System.out.println("✅ Users created: " + numberOfUsers);
        }

        //N * (1 + W/C) بما التعامل مع المحافظ و الداتا بيز تعتبر عمليات IO ممكن تاخد وقت كبير
        // 8 * (1 + 100/50) = 24
        // على فرض وقت التنفيذ هو 50 ميلي ثانية

        ExecutorService executor = Executors.newFixedThreadPool(24);
//        ExecutorService executor = Executors.newCachedThreadPool();
        // ✅ Step 2: Simulation
        for (int i = 1; i <= numberOfUsers; i++) {

            final long userId = i;

            for (int j = 0; j < ordersPerUser; j++) {

                executor.submit(() -> {
                    try {

                        long productId = ThreadLocalRandom.current().nextLong(1, 11);

                        String urlStr = "http://localhost:8080/shop/buy" +
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
                });
            }
        }

        executor.shutdown();

        try {
            executor.awaitTermination(15, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("✅ Success: " + success.get());
        System.out.println("❌ Fail: " + fail.get());
    }
}
