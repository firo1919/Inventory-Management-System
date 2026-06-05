package com.firomsa.inventory.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.firomsa.inventory.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("SELECT COUNT(p) FROM Product p WHERE p.quantity < p.lowStockThreshold")
    long countByQuantityLessThanLowStockThreshold();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.quantity = 0")
    long countByQuantityEquals(int quantity);

    @Query("SELECT p FROM Product p WHERE p.quantity < p.lowStockThreshold")
    List<Product> findByQuantityLessThanLowStockThreshold();

    @Query("SELECT COALESCE(SUM(p.quantity * p.sellingPrice), 0) FROM Product p")
    BigDecimal calculateTotalInventoryValue();

    @Query("SELECT COALESCE(SUM(p.quantity * p.sellingPrice), 0) FROM Product p JOIN p.categories c WHERE c.id = :categoryId")
    BigDecimal calculateInventoryValueByCategory(@Param("categoryId") UUID categoryId);
}
