package com.firomsa.inventory.v1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.firomsa.inventory.model.Product;
import com.firomsa.inventory.model.Restock;
import com.firomsa.inventory.model.Sale;
import com.firomsa.inventory.model.User;
import com.firomsa.inventory.repository.RestockRepository;
import com.firomsa.inventory.repository.SaleRepository;
import com.firomsa.inventory.v1.dto.TopProductDTO;
import com.firomsa.inventory.v1.dto.TransactionSummaryDTO;
import com.firomsa.inventory.v1.dto.TransactionTrendPointDTO;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceUnitTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private RestockRepository restockRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private Product createProduct(String name) {
        return Product.builder()
                .id(UUID.randomUUID())
                .name(name)
                .sku("SKU-" + name.toUpperCase())
                .sellingPrice(new BigDecimal("100.00"))
                .costPrice(new BigDecimal("70.00"))
                .quantity(50)
                .lowStockThreshold(5)
                .build();
    }

    private User createUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .username("testuser")
                .build();
    }

    @Test
    @DisplayName("should calculate transaction summary correctly")
    void shouldCalculateTransactionSummary() {
        // Arrange
        LocalDate start = LocalDate.of(2026, 7, 1);
        LocalDate end = LocalDate.of(2026, 7, 31);
        Product product = createProduct("Widget");
        User user = createUser();

        Sale sale1 = Sale.builder()
                .id(UUID.randomUUID())
                .product(product)
                .soldBy(user)
                .quantity(5)
                .salePrice(new BigDecimal("100.00"))
                .timestamp(LocalDateTime.of(2026, 7, 10, 10, 0))
                .build();

        Sale sale2 = Sale.builder()
                .id(UUID.randomUUID())
                .product(product)
                .soldBy(user)
                .quantity(3)
                .salePrice(new BigDecimal("150.00"))
                .timestamp(LocalDateTime.of(2026, 7, 15, 14, 0))
                .build();

        Restock restock1 = Restock.builder()
                .id(UUID.randomUUID())
                .product(product)
                .restockedBy(user)
                .quantity(20)
                .timestamp(LocalDateTime.of(2026, 7, 5, 9, 0))
                .build();

        when(saleRepository.findByTimestampBetween(any(), any())).thenReturn(List.of(sale1, sale2));
        when(restockRepository.findByTimestampBetween(any(), any())).thenReturn(List.of(restock1));

        // Act
        TransactionSummaryDTO summary = analyticsService.getSummary(start, end);

        // Assert
        assertThat(summary).isNotNull();
        assertThat(summary.getTotalSalesCount()).isEqualTo(2);
        assertThat(summary.getTotalUnitsSold()).isEqualTo(8);
        assertThat(summary.getTotalSalesRevenue()).isEqualByComparingTo(new BigDecimal("950.00")); // (5*100) + (3*150)
        assertThat(summary.getTotalRestocksCount()).isEqualTo(1);
        assertThat(summary.getTotalUnitsRestocked()).isEqualTo(20);
        assertThat(summary.getStartDate()).isEqualTo("2026-07-01");
        assertThat(summary.getEndDate()).isEqualTo("2026-07-31");
    }

    @Test
    @DisplayName("should calculate daily transaction trends correctly")
    void shouldCalculateDailyTransactionTrends() {
        // Arrange
        LocalDate start = LocalDate.of(2026, 7, 1);
        LocalDate end = LocalDate.of(2026, 7, 3);
        Product product = createProduct("Gadget");
        User user = createUser();

        Sale sale = Sale.builder()
                .id(UUID.randomUUID())
                .product(product)
                .soldBy(user)
                .quantity(2)
                .salePrice(new BigDecimal("50.00"))
                .timestamp(LocalDateTime.of(2026, 7, 1, 12, 0))
                .build();

        Restock restock = Restock.builder()
                .id(UUID.randomUUID())
                .product(product)
                .restockedBy(user)
                .quantity(10)
                .timestamp(LocalDateTime.of(2026, 7, 2, 8, 0))
                .build();

        when(saleRepository.findByTimestampBetween(any(), any())).thenReturn(List.of(sale));
        when(restockRepository.findByTimestampBetween(any(), any())).thenReturn(List.of(restock));

        // Act
        List<TransactionTrendPointDTO> trends = analyticsService.getTrends("DAILY", start, end);

        // Assert
        assertThat(trends).isNotEmpty();
        assertThat(trends).hasSize(3); // 2026-07-01, 2026-07-02, 2026-07-03

        TransactionTrendPointDTO day1 = trends.stream()
                .filter(t -> "2026-07-01".equals(t.getPeriod()))
                .findFirst()
                .orElse(null);

        assertThat(day1).isNotNull();
        assertThat(day1.getSalesCount()).isEqualTo(1);
        assertThat(day1.getUnitsSold()).isEqualTo(2);
        assertThat(day1.getSalesRevenue()).isEqualByComparingTo(new BigDecimal("100.00"));

        TransactionTrendPointDTO day2 = trends.stream()
                .filter(t -> "2026-07-02".equals(t.getPeriod()))
                .findFirst()
                .orElse(null);

        assertThat(day2).isNotNull();
        assertThat(day2.getRestocksCount()).isEqualTo(1);
        assertThat(day2.getUnitsRestocked()).isEqualTo(10);
    }

    @Test
    @DisplayName("should calculate top products by revenue correctly")
    void shouldCalculateTopProductsByRevenue() {
        // Arrange
        LocalDate start = LocalDate.of(2026, 7, 1);
        LocalDate end = LocalDate.of(2026, 7, 31);

        Product p1 = createProduct("Expensive Item");
        Product p2 = createProduct("Cheap Item");
        User user = createUser();

        Sale saleP1 = Sale.builder()
                .id(UUID.randomUUID())
                .product(p1)
                .soldBy(user)
                .quantity(2)
                .salePrice(new BigDecimal("500.00")) // Revenue = 1000
                .timestamp(LocalDateTime.of(2026, 7, 10, 10, 0))
                .build();

        Sale saleP2 = Sale.builder()
                .id(UUID.randomUUID())
                .product(p2)
                .soldBy(user)
                .quantity(50)
                .salePrice(new BigDecimal("5.00")) // Revenue = 250
                .timestamp(LocalDateTime.of(2026, 7, 11, 10, 0))
                .build();

        when(saleRepository.findByTimestampBetween(any(), any())).thenReturn(List.of(saleP1, saleP2));
        when(restockRepository.findByTimestampBetween(any(), any())).thenReturn(List.of());

        // Act
        List<TopProductDTO> topProducts = analyticsService.getTopProducts(10, start, end, "revenue");

        // Assert
        assertThat(topProducts).hasSize(2);
        assertThat(topProducts.get(0).getProductName()).isEqualTo("Expensive Item");
        assertThat(topProducts.get(0).getTotalRevenue()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(topProducts.get(1).getProductName()).isEqualTo("Cheap Item");
        assertThat(topProducts.get(1).getTotalRevenue()).isEqualByComparingTo(new BigDecimal("250.00"));
    }

    @Test
    @DisplayName("should calculate top products by quantity correctly")
    void shouldCalculateTopProductsByQuantity() {
        // Arrange
        LocalDate start = LocalDate.of(2026, 7, 1);
        LocalDate end = LocalDate.of(2026, 7, 31);

        Product p1 = createProduct("Expensive Item");
        Product p2 = createProduct("Cheap Item");
        User user = createUser();

        Sale saleP1 = Sale.builder()
                .id(UUID.randomUUID())
                .product(p1)
                .soldBy(user)
                .quantity(2)
                .salePrice(new BigDecimal("500.00"))
                .timestamp(LocalDateTime.of(2026, 7, 10, 10, 0))
                .build();

        Sale saleP2 = Sale.builder()
                .id(UUID.randomUUID())
                .product(p2)
                .soldBy(user)
                .quantity(50)
                .salePrice(new BigDecimal("5.00"))
                .timestamp(LocalDateTime.of(2026, 7, 11, 10, 0))
                .build();

        when(saleRepository.findByTimestampBetween(any(), any())).thenReturn(List.of(saleP1, saleP2));
        when(restockRepository.findByTimestampBetween(any(), any())).thenReturn(List.of());

        // Act
        List<TopProductDTO> topProducts = analyticsService.getTopProducts(10, start, end, "quantity");

        // Assert
        assertThat(topProducts).hasSize(2);
        assertThat(topProducts.get(0).getProductName()).isEqualTo("Cheap Item");
        assertThat(topProducts.get(0).getTotalQuantitySold()).isEqualTo(50);
        assertThat(topProducts.get(1).getProductName()).isEqualTo("Expensive Item");
        assertThat(topProducts.get(1).getTotalQuantitySold()).isEqualTo(2);
    }
}
