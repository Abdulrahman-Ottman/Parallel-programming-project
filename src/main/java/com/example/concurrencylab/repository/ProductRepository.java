package com.example.concurrencylab.repository;

import com.example.concurrencylab.model.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
       SELECT o.product.id
       FROM Order o
       GROUP BY o.product.id
       ORDER BY COUNT(o.id) DESC
       """)
    List<Long> findMostRequestedProducts(Pageable pageable);
}