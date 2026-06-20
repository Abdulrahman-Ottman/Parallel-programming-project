package com.example.concurrencylab.controller;

import com.example.concurrencylab.model.Product;
import com.example.concurrencylab.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(
            ProductService productService
    ) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public Product getProduct(
            @PathVariable Long id
    ) {

        return productService.getProduct(id);
    }
}
