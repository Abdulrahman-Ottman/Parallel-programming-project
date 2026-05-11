package com.example.concurrencylab.controller;

import com.example.concurrencylab.model.Order;
import com.example.concurrencylab.service.OrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop")
public class ShopController {

    private final OrderService orderService;

    // ✅ Constructor Injection
    public ShopController(OrderService orderService) {
        this.orderService = orderService;
    }
    @GetMapping("/orders")
    public Object getOrders() {
        return orderService.getOrders();
    }

    @GetMapping("/discount/count")
    public int count() {
        return orderService.getDiscountService().getCurrentUses();
    }

    @PostMapping("/buy")
    public Order buy(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) double discountValue

    ) {
        return orderService.buy(userId, productId, code , discountValue);
    }

}