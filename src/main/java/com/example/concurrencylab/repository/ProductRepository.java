package com.example.concurrencylab.repository;

import com.example.concurrencylab.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}