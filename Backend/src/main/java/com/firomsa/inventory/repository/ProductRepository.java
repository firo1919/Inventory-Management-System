package com.firomsa.inventory.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.firomsa.inventory.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @EntityGraph(attributePaths = {"imageKeys"})
    @Query("SELECT p FROM Product p")
    List<Product> findAllWithImages();

    @EntityGraph(attributePaths = {"imageKeys"})
    @Query("SELECT p FROM Product p")
    Page<Product> findAllWithImages(Pageable pageable);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.quantity < p.lowStockThreshold")
    long countByQuantityLessThanLowStockThreshold();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.quantity = 0")
    long countByQuantityEquals(int quantity);

    @Query("SELECT p FROM Product p WHERE p.quantity < p.lowStockThreshold")
    List<Product> findByQuantityLessThanLowStockThreshold();

    @Query("SELECT p FROM Product p WHERE p.quantity < p.lowStockThreshold")
    Page<Product> findByQuantityLessThanLowStockThreshold(Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.quantity * p.sellingPrice), 0) FROM Product p")
    BigDecimal calculateTotalInventoryValue();

    @Query("SELECT COALESCE(SUM(p.quantity * p.sellingPrice), 0) FROM Product p JOIN p.categories c WHERE c.id = :categoryId")
    BigDecimal calculateInventoryValueByCategory(@Param("categoryId") UUID categoryId);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE Product p SET p.quantity = p.quantity - :qty WHERE p.id = :id AND p.quantity >= :qty")
    int decrementQuantity(@Param("id") UUID id, @Param("qty") int qty);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE Product p SET p.quantity = p.quantity + :qty WHERE p.id = :id")
    int incrementQuantity(@Param("id") UUID id, @Param("qty") int qty);
}
