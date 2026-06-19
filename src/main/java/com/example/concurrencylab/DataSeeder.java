package com.example.concurrencylab;

import com.example.concurrencylab.model.Product;
import com.example.concurrencylab.model.User;
import com.example.concurrencylab.repository.ProductRepository;
import com.example.concurrencylab.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public DataSeeder(UserRepository userRepository, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        // 1. تعبئة جدول users [id, email, name]
        if (userRepository.count() == 0) {
            System.out.println("⚙️ Seeding Users into SQLite...");
            for (int i = 1; i <= 10; i++) {
                User user = new User();
                user.setName("User " + i);
                user.setEmail("user" + i + "@example.com"); // إضافة الإيميل حسب الـ Schema الخاصة بك
                userRepository.save(user);
            }
        }

        // 2. تعبئة جدول products [id, name, price, stock]
        if (productRepository.count() == 0) {
            System.out.println("⚙️ Seeding Products into SQLite...");
            for (int i = 1; i <= 5; i++) {
                Product product = new Product();
                product.setName("Product " + i);
                product.setPrice(150.0);
                product.setStock(1000); // وضعنا 1000 قطعة في المخزون لكي لا ينفد أثناء المحاكاة
                productRepository.save(product);
            }
            System.out.println("✅ SQLite Database is now populated with mock data!");
        }
    }
}