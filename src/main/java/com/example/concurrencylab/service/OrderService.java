package com.example.concurrencylab.service;

import com.example.concurrencylab.model.Order;
import com.example.concurrencylab.model.Product;
import com.example.concurrencylab.model.User;
import com.example.concurrencylab.repository.OrderRepository;
import com.example.concurrencylab.repository.ProductRepository;
import com.example.concurrencylab.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private final DiscountService discountService = new DiscountService();

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }
    private List<String> orders = new ArrayList<>();

    public Order buy(Long userId, Long productId, String discountCode , double discountValue) {

        boolean discountApplied = false;

        if (discountCode != null && discountCode.equals("SAVE10")) {
            discountApplied = discountService.applyDiscount(userId);
        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        User user = userRepository.findById(userId).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();

        if (product.getStock() <= 0) {
            throw new RuntimeException("Out of stock");
        }

        product.setStock(product.getStock() - 1);

        Order order = new Order();
        order.setUser(user);
        order.setProduct(product);
        order.setDiscountApplied(discountApplied);
        if (discountApplied){
            order.setDiscountValue(discountValue);
        }
        return orderRepository.save(order);
    }

    public List<String> getOrders() {
        return orders;
    }

    public DiscountService getDiscountService() {
        return discountService;
    }
}