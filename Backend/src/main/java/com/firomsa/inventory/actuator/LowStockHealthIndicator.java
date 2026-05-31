package com.firomsa.inventory.actuator;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import com.firomsa.inventory.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class LowStockHealthIndicator implements HealthIndicator {

    private final ProductRepository productRepository;

    @Override
    public Health health() {
        long lowStockCount = productRepository.countByQuantityLessThanLowStockThreshold();
        long outOfStockCount = productRepository.countByQuantityEquals(0);

        Health.Builder healthBuilder;
        if (lowStockCount > 0) {
            healthBuilder = Health.down();
        } else {
            healthBuilder = Health.up();
        }

        return healthBuilder
                .withDetail("lowStockCount", lowStockCount)
                .withDetail("outOfStockCount", outOfStockCount)
                .withDetail("message", lowStockCount > 0
                        ? lowStockCount + " products are below their low stock threshold"
                        : "All products have sufficient stock")
                .build();
    }
}
