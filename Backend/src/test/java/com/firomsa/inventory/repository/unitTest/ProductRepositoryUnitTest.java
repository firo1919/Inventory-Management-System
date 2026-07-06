package com.firomsa.inventory.repository.unitTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.firomsa.inventory.config.JpaAuditingConfig;
import com.firomsa.inventory.model.Category;
import com.firomsa.inventory.model.Product;
import com.firomsa.inventory.model.Sale;
import com.firomsa.inventory.model.User;
import com.firomsa.inventory.repository.CategoryRepository;
import com.firomsa.inventory.repository.ProductRepository;
import com.firomsa.inventory.repository.SaleRepository;
import com.firomsa.inventory.repository.UserRepository;
import com.firomsa.inventory.support.SharedContainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
public class ProductRepositoryUnitTest {

    @ServiceConnection
    static PostgreSQLContainer postgres = SharedContainers.POSTGRES;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private UserRepository userRepository;

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
        assertThat(totalValue).isEqualByComparingTo(BigDecimal.valueOf(2000));
    }

    @Test
    @DisplayName("should return zero for empty inventory")
    void shouldReturnZeroForEmptyInventory() {
        // Act
        BigDecimal totalValue = productRepository.calculateTotalInventoryValue();

        // Assert
        assertThat(totalValue).isEqualByComparingTo(BigDecimal.ZERO);
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
        Product product2 = createProduct("Chair", "FURN-001", 10, 5, BigDecimal.valueOf(200));
        productRepository.save(product1);
        productRepository.save(product2);

        // Category owns the join table, so associate from the owning side.
        electronics.setProducts(new java.util.HashSet<>(java.util.Set.of(product1)));
        furniture.setProducts(new java.util.HashSet<>(java.util.Set.of(product2)));
        categoryRepository.save(electronics);
        categoryRepository.save(furniture);

        // Act
        BigDecimal electronicsValue = productRepository.calculateInventoryValueByCategory(electronics.getId());
        BigDecimal furnitureValue = productRepository.calculateInventoryValueByCategory(furniture.getId());

        // Assert
        assertThat(electronicsValue).isEqualByComparingTo(BigDecimal.valueOf(5000)); // 5 * 1000
        assertThat(furnitureValue).isEqualByComparingTo(BigDecimal.valueOf(2000)); // 10 * 200
    }

    @Test
    @DisplayName("should find sales between dates")
    void shouldFindSalesBetweenDates() {
        // Arrange
        User seller = userRepository.save(User.builder().firstName("Jane").lastName("Seller")
                .username("jane_seller").password("password123").email("jane.seller@example.com")
                .phone("0911000000").build());
        Product product = productRepository
                .save(createProduct("Monitor", "MON-001", 20, 5, BigDecimal.valueOf(300)));

        LocalDateTime january = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime february = LocalDateTime.of(2026, 2, 15, 10, 0);
        LocalDateTime march = LocalDateTime.of(2026, 3, 15, 10, 0);

        saveSaleAt(seller, product, 2, BigDecimal.valueOf(600.0), january);
        saveSaleAt(seller, product, 1, BigDecimal.valueOf(300.0), february);
        saveSaleAt(seller, product, 3, BigDecimal.valueOf(900.0), march);

        // Act: window covers January and February but excludes March
        List<Sale> sales = saleRepository.findByTimestampBetween(
                LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 2, 28, 23, 59));

        // Assert
        assertThat(sales).hasSize(2)
                .extracting(Sale::getTimestamp)
                .containsExactlyInAnyOrder(january, february);
    }

    // timestamp is a @CreatedDate field set on insert, so persist first then
    // override it to place the sale at a deterministic point in time.
    private void saveSaleAt(User seller, Product product, int quantity, BigDecimal salePrice,
            LocalDateTime timestamp) {
        Sale sale = saleRepository.save(Sale.builder().soldBy(seller).product(product)
                .quantity(quantity).salePrice(salePrice).build());
        sale.setTimestamp(timestamp);
        saleRepository.save(sale);
    }
}
