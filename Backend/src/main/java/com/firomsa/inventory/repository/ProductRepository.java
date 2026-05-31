package com.firomsa.inventory.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.firomsa.inventory.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("SELECT COUNT(p) FROM Product p WHERE p.quantity < p.lowStockThreshold")
    long countByQuantityLessThanLowStockThreshold();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.quantity = 0")
    long countByQuantityEquals(int quantity);
}
