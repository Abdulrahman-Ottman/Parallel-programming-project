package com.example.concurrencylab.service;

import com.example.concurrencylab.model.Order;
import com.example.concurrencylab.model.Product;
import com.example.concurrencylab.model.User;
import com.example.concurrencylab.repository.OrderRepository;
import com.example.concurrencylab.repository.ProductRepository;
import com.example.concurrencylab.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryTransactionService {

    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;
    private final UserRepository userRepo;

    public InventoryTransactionService(ProductRepository productRepo,
                                       OrderRepository orderRepo,
                                       UserRepository userRepo) {
        this.productRepo = productRepo;
        this.orderRepo   = orderRepo;
        this.userRepo    = userRepo;
    }


    @Transactional
    public Order executePurchase(Long productId, int quantity, Long userId) {

        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        // قراءة آمنة للمخزون — بعد أخذ القفل مباشرة
        if (product.getStock() < quantity) {
            throw new RuntimeException(
                "المخزون غير كافٍ: متوفر " + product.getStock());
        }

        // تحديث المخزون — لا Race Condition ممكنة هنا
        product.setStock(product.getStock() - quantity);
        productRepo.save(product);

        User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Order order = new Order();
        order.setProduct(product);
        order.setUser(user);

        System.out.println("طلب مؤكد — مخزون متبقٍّ: " + product.getStock());
        return orderRepo.save(order);
    }
}
