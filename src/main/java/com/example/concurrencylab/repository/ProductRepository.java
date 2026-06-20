package com.example.concurrencylab.repository;

import com.example.concurrencylab.model.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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


    //these three lines are for Transaction Integrity (طلب 8)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Product findByIdForUpdate(Long id);
}