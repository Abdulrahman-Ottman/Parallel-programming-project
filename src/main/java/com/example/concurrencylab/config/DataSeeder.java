package com.example.concurrencylab.config;

import com.example.concurrencylab.model.Product;
import com.example.concurrencylab.model.User;
import com.example.concurrencylab.repository.ProductRepository;
import com.example.concurrencylab.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * يحشو قاعدة البيانات بالمنتجات والمستخدمين عند الـ startup
 * إذا كانوا غير موجودين — يضمن إن الـ IDs من 1-10 دائماً موجودة.
 */
@Component
@Order(1) // يشتغل قبل CommandLineRunner في ConcurrencylabApplication
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public DataSeeder(ProductRepository productRepository,
                      UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository    = userRepository;
    }

    @Override
    public void run(String... args) {
        seedProducts();
        seedUsers();
    }

    private void seedProducts() {
        if (productRepository.count() == 0) {
            System.out.println("[DataSeeder] Seeding 10 products...");
            for (int i = 1; i <= 10; i++) {
                Product p = new Product();
                p.setName("Product " + i);
                p.setPrice(10.0 * i);
                p.setStock(100);
                productRepository.save(p);
            }
            System.out.println("[DataSeeder] Products seeded.");
        } else {
            System.out.println("[DataSeeder] Products already exist, skipping.");
        }
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            System.out.println("[DataSeeder] Seeding 10 users...");
            for (int i = 1; i <= 10; i++) {
                User u = new User();
                u.setName("User " + i);
                u.setEmail("user" + i + "@test.com");
                userRepository.save(u);
            }
            System.out.println("[DataSeeder] Users seeded.");
        } else {
            System.out.println("[DataSeeder] Users already exist, skipping.");
        }
    }
}
