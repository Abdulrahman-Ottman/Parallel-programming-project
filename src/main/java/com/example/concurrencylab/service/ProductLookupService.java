package com.example.concurrencylab.service;

import com.example.concurrencylab.aspect.TrackOrderLatency;
import com.example.concurrencylab.model.Product;
import com.example.concurrencylab.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductLookupService {

    private final ProductRepository productRepository;

    public ProductLookupService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @TrackOrderLatency(scenario = "CACHE_MISS")
    public Product loadFromDatabase(Long id) {


        return productRepository.findById(id)
                .orElseThrow();
    }

    @TrackOrderLatency(scenario = "CACHE_HIT")
    public Product loadFromCache(Product product) {
        return product;
    }
    public Long getMostRequestedProductId() {

        List<Long> ids = productRepository.findMostRequestedProducts(
                PageRequest.of(0, 1)
        );

        return ids.isEmpty() ? null : ids.get(0);
    }
}