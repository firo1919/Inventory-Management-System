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
import com.firomsa.inventory.model.Product;
import com.firomsa.inventory.model.Restock;
import com.firomsa.inventory.model.User;
import com.firomsa.inventory.repository.ProductRepository;
import com.firomsa.inventory.repository.RestockRepository;
import com.firomsa.inventory.repository.UserRepository;
import com.firomsa.inventory.support.SharedContainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
public class RestockRepositoryUnitTest {

    @ServiceConnection
    static PostgreSQLContainer postgres = SharedContainers.POSTGRES;

    @Autowired
    private RestockRepository restockRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("should find restocks by timestamp between date range")
    void shouldFindRestocksByTimestampBetween() {
        // Arrange
        User user = userRepository.save(User.builder()
                .firstName("Test")
                .lastName("Manager")
                .username("manager_test")
                .password("password123")
                .email("manager.test@example.com")
                .phone("1234567891")
                .build());

        Product product = productRepository.save(Product.builder()
                .name("Keyboard")
                .sku("KB-001")
                .sellingPrice(new BigDecimal("50.00"))
                .costPrice(new BigDecimal("30.00"))
                .quantity(100)
                .lowStockThreshold(10)
                .build());

        Restock restock = Restock.builder()
                .product(product)
                .restockedBy(user)
                .quantity(25)
                .timestamp(LocalDateTime.now())
                .build();

        restockRepository.save(restock);

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        // Act
        List<Restock> results = restockRepository.findByTimestampBetween(start, end);

        // Assert
        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(r -> r.getQuantity() == 25 && r.getProduct().getSku().equals("KB-001"));
    }
}
