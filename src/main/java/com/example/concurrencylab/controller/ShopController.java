package com.example.concurrencylab.controller;

import com.example.concurrencylab.model.Order;
import com.example.concurrencylab.model.Product;
import com.example.concurrencylab.service.OrderService;
import com.example.concurrencylab.service.ProductService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop")
public class ShopController {

    private final OrderService orderService;

    // ✅ Constructor Injection
    public ShopController(OrderService orderService) {
        this.orderService = orderService;
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

    @PostMapping("/buy/no-queue")
    public Order buyWithoutQueue(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) double discountValue

    ) {
        return orderService.buyWithoutQueue(userId, productId, code , discountValue);
    }
    @PostMapping("/buy/no-transaction")
    public Order buyNoTransaction(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) double discountValue
    ) {
        return orderService.buyNoTransaction(userId, productId, code, discountValue);
    }
    @GetMapping("/tasks/executed")
    public int tasksExecuted() {

        return orderService.getExecutedTasks();
    }



}