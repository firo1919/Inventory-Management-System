package com.firomsa.inventory.v1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
}
