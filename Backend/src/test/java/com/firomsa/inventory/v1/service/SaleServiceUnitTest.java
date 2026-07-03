package com.firomsa.inventory.v1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.firomsa.inventory.exception.ResourceNotFoundException;
import com.firomsa.inventory.model.Product;
import com.firomsa.inventory.model.Sale;
import com.firomsa.inventory.model.User;
import com.firomsa.inventory.repository.ProductRepository;
import com.firomsa.inventory.repository.SaleRepository;
import com.firomsa.inventory.repository.UserRepository;
import com.firomsa.inventory.v1.dto.SaleRequestDTO;
import com.firomsa.inventory.v1.dto.SaleResponseDTO;
import com.firomsa.inventory.v1.mapper.SaleMapper;

@ExtendWith(MockitoExtension.class)
public class SaleServiceUnitTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SaleMapper saleMapper;
    @Mock
    private SaleRepository saleRepository;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private SaleService saleService;

    Product getProduct() {
        return Product.builder().id(UUID.randomUUID()).name("Test Product").quantity(10).build();
    }

    User getUser() {
        return User.builder().id(UUID.randomUUID()).email("user@example.com").build();
    }

    @Test
    void shouldCreateASale() {
        // Arrange
        var request = SaleRequestDTO.builder().productId(UUID.randomUUID()).quantity(5)
                .salePrice(10.0).build();
        var response = SaleResponseDTO.builder().productId(request.getProductId())
                .quantity(request.getQuantity()).salePrice(request.getSalePrice())
                .message("Sale recorded successfully").build();
        var product = getProduct();
        var user = getUser();
        var sale = Sale.builder().product(product).quantity(request.getQuantity()).salePrice(request.getSalePrice())
                .soldBy(user)
                .build();
        when(saleMapper.toDTO(any())).thenReturn(response);
        when(saleMapper.toModel(request)).thenReturn(sale);
        when(saleRepository.save(any())).thenReturn(sale);
        when(productRepository.findById(request.getProductId())).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        // Act
        var result = saleService.createSale(request, "user@example.com");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Sale recorded successfully");
        assertThat(result.getQuantity()).isEqualTo(request.getQuantity());
        assertThat(result.getSalePrice()).isEqualTo(request.getSalePrice());
        assertThat(result.getProductId()).isEqualTo(request.getProductId());
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        // Arrange
        var request = SaleRequestDTO.builder().productId(UUID.randomUUID()).quantity(5)
                .salePrice(10.0).build();
        when(productRepository.findById(request.getProductId())).thenReturn(Optional.empty());

        // Act & Assert
        var exception = assertThrows(ResourceNotFoundException.class, () -> {
            saleService.createSale(request, "user@example.com");
        });
        assertThat(exception.getMessage()).contains("Product not found with id: " + request.getProductId());
    }

    @Test
    void shouldThrowExceptionWhenInsufficientStock() {
        // Arrange
        var request = SaleRequestDTO.builder().productId(UUID.randomUUID()).quantity(15)
                .salePrice(10.0).build();
        var product = getProduct();
        var user = getUser();
        when(productRepository.findById(request.getProductId())).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        // Act & Assert
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            saleService.createSale(request, "user@example.com");
        });
        assertThat(exception.getMessage()).contains("Insufficient stock for product: " + product.getName());
    }

    @Test
    void shouldReturnAllSales() {
        // Arrange
        var product = getProduct();
        var user = getUser();
        var firstSale = Sale.builder().id(UUID.randomUUID()).product(product).soldBy(user)
                .quantity(2).salePrice(12.0).build();
        var secondSale = Sale.builder().id(UUID.randomUUID()).product(product).soldBy(user)
                .quantity(4).salePrice(15.0).build();

        var firstResponse = SaleResponseDTO.builder().productId(product.getId()).quantity(2)
                .salePrice(12.0).build();
        var secondResponse = SaleResponseDTO.builder().productId(product.getId()).quantity(4)
                .salePrice(15.0).build();

        when(saleRepository.findAll()).thenReturn(List.of(firstSale, secondSale));
        when(saleMapper.toDTO(eq(firstSale))).thenReturn(firstResponse);
        when(saleMapper.toDTO(eq(secondSale))).thenReturn(secondResponse);

        // Act
        var result = saleService.getAllSales();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getQuantity()).isEqualTo(2);
        assertThat(result.get(1).getQuantity()).isEqualTo(4);
    }

    @Test
    void shouldReturnSaleById() {
        // Arrange
        var saleId = UUID.randomUUID();
        var product = getProduct();
        var user = getUser();
        var sale = Sale.builder().id(saleId).product(product).soldBy(user).quantity(3)
                .salePrice(11.0).build();
        var response = SaleResponseDTO.builder().productId(product.getId()).quantity(3)
                .salePrice(11.0).build();

        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        when(saleMapper.toDTO(sale)).thenReturn(response);

        // Act
        var result = saleService.getSaleById(saleId);

        // Assert
        assertThat(result.getQuantity()).isEqualTo(3);
        assertThat(result.getSalePrice()).isEqualTo(11.0);
    }

    @Test
    void shouldThrowExceptionWhenSaleNotFoundById() {
        // Arrange
        var saleId = UUID.randomUUID();
        when(saleRepository.findById(saleId)).thenReturn(Optional.empty());

        // Act & Assert
        var exception = assertThrows(ResourceNotFoundException.class, () -> {
            saleService.getSaleById(saleId);
        });
        assertThat(exception.getMessage()).contains("Sale not found with id: " + saleId);
    }

    @Test
    void shouldReturnSalesByEmployeeEmail() {
        // Arrange
        var email = "employee@example.com";
        var product = getProduct();
        var user = getUser();
        var sale = Sale.builder().id(UUID.randomUUID()).product(product).soldBy(user).quantity(6)
                .salePrice(20.0).build();
        var response = SaleResponseDTO.builder().productId(product.getId()).quantity(6)
                .salePrice(20.0).build();

        when(saleRepository.findBySoldByEmail(email)).thenReturn(List.of(sale));
        when(saleMapper.toDTO(sale)).thenReturn(response);

        // Act
        var result = saleService.getSalesByEmployee(email);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuantity()).isEqualTo(6);
    }

    @Test
    void shouldDeleteSaleById() {
        var saleId = UUID.randomUUID();
        var sale = Sale.builder().id(saleId).build();
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
        doNothing().when(saleRepository).delete(sale);

        saleService.deleteSaleById(saleId);

        verify(saleRepository).delete(sale);
    }

    @Test
    void shouldThrowExceptionWhenDeletingUnknownSale() {
        var saleId = UUID.randomUUID();
        when(saleRepository.findById(saleId)).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> {
            saleService.deleteSaleById(saleId);
        });

        assertThat(exception.getMessage()).contains("Sale not found with id: " + saleId);
    }

    @Test
    void shouldTriggerNotificationWhenLowStockAfterSale() {
        // Arrange
        var request = SaleRequestDTO.builder().productId(UUID.randomUUID()).quantity(8)
                .salePrice(10.0).build();
        var response = SaleResponseDTO.builder().productId(request.getProductId())
                .quantity(request.getQuantity()).salePrice(request.getSalePrice())
                .message("Sale recorded successfully").build();
        var product = Product.builder().id(request.getProductId()).name("Test Product")
                .quantity(10).lowStockThreshold(5).build();
        var user = getUser();
        var sale = Sale.builder().product(product).quantity(request.getQuantity())
                .salePrice(request.getSalePrice()).soldBy(user).build();

        when(saleMapper.toDTO(any())).thenReturn(response);
        when(saleMapper.toModel(request)).thenReturn(sale);
        when(saleRepository.save(any())).thenReturn(sale);
        when(productRepository.findById(request.getProductId())).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        // Act
        saleService.createSale(request, "user@example.com");

        // Assert
        verify(notificationService).sendLowStockAlertIfNeeded(product);
    }

    @Test
    void shouldNotTriggerNotificationWhenStockStillSufficient() {
        // Arrange
        var request = SaleRequestDTO.builder().productId(UUID.randomUUID()).quantity(2)
                .salePrice(10.0).build();
        var response = SaleResponseDTO.builder().productId(request.getProductId())
                .quantity(request.getQuantity()).salePrice(request.getSalePrice())
                .message("Sale recorded successfully").build();
        var product = Product.builder().id(request.getProductId()).name("Test Product")
                .quantity(10).lowStockThreshold(5).build();
        var user = getUser();
        var sale = Sale.builder().product(product).quantity(request.getQuantity())
                .salePrice(request.getSalePrice()).soldBy(user).build();

        when(saleMapper.toDTO(any())).thenReturn(response);
        when(saleMapper.toModel(request)).thenReturn(sale);
        when(saleRepository.save(any())).thenReturn(sale);
        when(productRepository.findById(request.getProductId())).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        // Act
        saleService.createSale(request, "user@example.com");

        // Assert
        verify(notificationService).sendLowStockAlertIfNeeded(product);
    }

    @Test
    void createSale_WhenEmailNotificationFails_ShouldStillCompleteSaleTransaction() {
        // Arrange
        var request = SaleRequestDTO.builder().productId(UUID.randomUUID()).quantity(3)
                .salePrice(15.0).build();
        var response = SaleResponseDTO.builder().productId(request.getProductId())
                .quantity(request.getQuantity()).salePrice(request.getSalePrice())
                .message("Sale recorded successfully").build();
        var product = Product.builder().id(request.getProductId()).name("Test Product")
                .quantity(10).lowStockThreshold(8).build();
        var user = getUser();
        var sale = Sale.builder().product(product).quantity(request.getQuantity())
                .salePrice(request.getSalePrice()).soldBy(user).build();

        when(saleMapper.toDTO(any())).thenReturn(response);
        when(saleMapper.toModel(request)).thenReturn(sale);
        when(saleRepository.save(any())).thenReturn(sale);
        when(productRepository.findById(request.getProductId())).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("SMTP connection failed"))
                .when(notificationService).sendLowStockAlertIfNeeded(any());

        // Act
        var result = saleService.createSale(request, "user@example.com");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Sale recorded successfully");
        assertThat(product.getQuantity()).isEqualTo(7);
        verify(productRepository).save(product);
        verify(saleRepository).save(any());
        verify(notificationService).sendLowStockAlertIfNeeded(product);
    }
}
