package com.firomsa.inventory.metrics;

import org.springframework.stereotype.Component;

import com.firomsa.inventory.repository.CategoryRepository;
import com.firomsa.inventory.repository.ProductRepository;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryMetrics {

    private final MeterRegistry meterRegistry;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @PostConstruct
    public void registerMetrics() {
        Gauge.builder("inventory.products.total", productRepository, repo -> repo.count())
                .description("Total number of products in inventory")
                .register(meterRegistry);

        Gauge.builder("inventory.products.low_stock", productRepository,
                        repo -> repo.countByQuantityLessThanLowStockThreshold())
                .description("Number of products below their low stock threshold")
                .register(meterRegistry);

        Gauge.builder("inventory.products.out_of_stock", productRepository,
                        repo -> repo.countByQuantityEquals(0))
                .description("Number of products with zero quantity")
                .register(meterRegistry);

        Gauge.builder("inventory.categories.count", categoryRepository, repo -> repo.count())
                .description("Total number of categories")
                .register(meterRegistry);

        log.info("Inventory metrics registered successfully");
    }
}
