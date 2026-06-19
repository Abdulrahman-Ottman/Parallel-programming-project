package com.example.concurrencylab.service;

import com.example.concurrencylab.aspect.TrackOrderLatency;
import com.example.concurrencylab.model.Product;
import com.example.concurrencylab.repository.ProductRepository;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final IMap<String, Product> productCache;
    private final ProductLookupService lookup;
    public ProductService(ProductRepository productRepository,
                          HazelcastInstance hazelcastInstance, ProductLookupService lookup) {
        this.productRepository = productRepository;
        this.productCache = hazelcastInstance.getMap("products");
        this.lookup = lookup;
    }

    public Product getProduct(Long productId) {
        String key = "product:" + productId;

        Product cached = productCache.get(key);
        if (cached != null) {
            System.out.println("CACHE HIT -> " + productId);
            System.out.println("Cached product: " + cached.getName());
            System.out.println("Cached product price: " + cached.getPrice());
            return lookup.loadFromCache(
                    cached
            );        }

        System.out.println("CACHE MISS -> " + productId);

        Product product = lookup.loadFromDatabase(productId);
        productCache.put(key, product);
        return product;
    }

    @TrackOrderLatency(scenario = "DB")
    public Product getProductFromDatabase(Long productId) {
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return productRepository.findById(productId)
                .orElseThrow();
    }
    public Long getMostRequestedProductId() {
        List<Long> ids = productRepository.findMostRequestedProducts(
                PageRequest.of(0, 1)
        );
        return ids.isEmpty() ? null : ids.get(0);
    }
    public long preloadMostSoldProduct() {

        Long productId =
               getMostRequestedProductId();

        if(productId == null) {
            return 1;
        }

        Product product =
                lookup.loadFromDatabase(productId);



        System.out.println(
                "Preloaded product "
                        + productId
        );
        return productId;
    }


}