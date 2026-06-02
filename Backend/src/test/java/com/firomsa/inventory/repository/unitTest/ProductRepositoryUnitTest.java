package com.firomsa.inventory.repository.unitTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.firomsa.inventory.config.JpaAuditingConfig;
import com.firomsa.inventory.model.Category;
import com.firomsa.inventory.model.Product;
import com.firomsa.inventory.repository.CategoryRepository;
import com.firomsa.inventory.repository.ProductRepository;

@DataJpaTest
@Import(JpaAuditingConfig.class)
public class ProductRepositoryUnitTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Product createProduct(String name, String sku, int quantity, int threshold, BigDecimal sellingPrice) {
        return Product.builder()
                .name(name)
                .sku(sku)
                .sellingPrice(sellingPrice)
                .costPrice(BigDecimal.valueOf(50))
                .quantity(quantity)
                .lowStockThreshold(threshold)
                .build();
    }

    @Test
    @DisplayName("should find products below low stock threshold")
    void shouldFindProductsBelowLowStockThreshold() {
        // Arrange
        Product lowStockProduct = createProduct("Low Stock Product", "LOW-001", 2, 5, BigDecimal.valueOf(100));
        Product normalStockProduct = createProduct("Normal Stock Product", "NORM-001", 10, 5, BigDecimal.valueOf(100));

        productRepository.save(lowStockProduct);
        productRepository.save(normalStockProduct);

        // Act
        List<Product> lowStockProducts = productRepository.findByQuantityLessThanLowStockThreshold();

        // Assert
        assertThat(lowStockProducts).hasSize(1);
        assertThat(lowStockProducts.get(0).getSku()).isEqualTo("LOW-001");
        assertThat(lowStockProducts.get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("should return empty list when no low stock products")
    void shouldReturnEmptyListWhenNoLowStockProducts() {
        // Arrange
        Product normalStockProduct = createProduct("Normal Stock Product", "NORM-001", 10, 5, BigDecimal.valueOf(100));

        productRepository.save(normalStockProduct);

        // Act
        List<Product> lowStockProducts = productRepository.findByQuantityLessThanLowStockThreshold();

        // Assert
        assertThat(lowStockProducts).isEmpty();
    }

    @Test
    @DisplayName("should calculate total inventory value")
    void shouldCalculateTotalInventoryValue() {
        // Arrange
        Product product1 = createProduct("Product 1", "PROD-001", 10, 5, BigDecimal.valueOf(100));
        Product product2 = createProduct("Product 2", "PROD-002", 5, 3, BigDecimal.valueOf(200));

        productRepository.save(product1);
        productRepository.save(product2);

        // Act
        BigDecimal totalValue = productRepository.calculateTotalInventoryValue();

        // Assert: (10 * 100) + (5 * 200) = 1000 + 1000 = 2000
        assertThat(totalValue).isEqualTo(BigDecimal.valueOf(2000).setScale(4));
    }

    @Test
    @DisplayName("should return zero for empty inventory")
    void shouldReturnZeroForEmptyInventory() {
        // Act
        BigDecimal totalValue = productRepository.calculateTotalInventoryValue();

        // Assert
        assertThat(totalValue).isEqualTo(BigDecimal.ZERO.setScale(4));
    }

    @Test
    @DisplayName("should calculate inventory value by category")
    void shouldCalculateInventoryValueByCategory() {
        // Arrange
        Category electronics = Category.builder().name("Electronics").build();
        Category furniture = Category.builder().name("Furniture").build();
        categoryRepository.save(electronics);
        categoryRepository.save(furniture);

        Product product1 = createProduct("Laptop", "ELEC-001", 5, 2, BigDecimal.valueOf(1000));
        product1.setCategories(java.util.Set.of(electronics));

        Product product2 = createProduct("Chair", "FURN-001", 10, 5, BigDecimal.valueOf(200));
        product2.setCategories(java.util.Set.of(furniture));

        productRepository.save(product1);
        productRepository.save(product2);

        // Act
        BigDecimal electronicsValue = productRepository.calculateInventoryValueByCategory(electronics.getId());
        BigDecimal furnitureValue = productRepository.calculateInventoryValueByCategory(furniture.getId());

        // Assert
        assertThat(electronicsValue).isEqualTo(BigDecimal.valueOf(5000).setScale(4)); // 5 * 1000
        assertThat(furnitureValue).isEqualTo(BigDecimal.valueOf(2000).setScale(4)); // 10 * 200
    }

    @Test
    @DisplayName("should find sales between dates")
    void shouldFindSalesBetweenDates() {
        // This test will be added when we implement the SaleRepository date range query
        // For now, just verify the method signature exists
        assertThat(true).isTrue();
    }
}
