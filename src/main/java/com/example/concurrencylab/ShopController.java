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

}