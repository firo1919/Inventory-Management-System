package com.firomsa.inventory.v1.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.firomsa.inventory.config.AdminConfig;
import com.firomsa.inventory.model.Product;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceUnitTest {

    @Mock
    private EmailService emailService;

    @Mock
    private AdminConfig adminConfig;

    @InjectMocks
    private NotificationService notificationService;

    private Product createProduct(String name, String sku, int quantity, int threshold) {
        return Product.builder()
                .name(name)
                .sku(sku)
                .quantity(quantity)
                .lowStockThreshold(threshold)
                .sellingPrice(BigDecimal.valueOf(100))
                .costPrice(BigDecimal.valueOf(50))
                .build();
    }

    @Test
    void shouldSendLowStockAlertEmail() {
        // Arrange
        Product lowStockProduct = createProduct("Test Product", "SKU-001", 2, 5);
        when(adminConfig.getEmail()).thenReturn("admin@example.com");

        // Act
        notificationService.sendLowStockAlert(lowStockProduct);

        // Assert
        verify(emailService).sendLowStockAlert("admin@example.com", lowStockProduct);
    }

    @Test
    void shouldNotSendAlertWhenStockSufficient() {
        // Arrange
        Product normalStockProduct = createProduct("Test Product", "SKU-002", 10, 5);

        // Act
        notificationService.sendLowStockAlertIfNeeded(normalStockProduct);

        // Assert
        verify(emailService, never()).sendLowStockAlert(any(), any());
    }

    @Test
    void shouldIncludeProductDetailsInEmail() {
        // Arrange
        Product lowStockProduct = createProduct("Widget", "WDG-123", 1, 10);
        when(adminConfig.getEmail()).thenReturn("admin@test.com");

        // Act
        notificationService.sendLowStockAlert(lowStockProduct);

        // Assert
        verify(emailService).sendLowStockAlert("admin@test.com", lowStockProduct);
    }
}
