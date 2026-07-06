package com.firomsa.inventory.v1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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
import com.firomsa.inventory.model.Restock;
import com.firomsa.inventory.model.User;
import com.firomsa.inventory.repository.ProductRepository;
import com.firomsa.inventory.repository.RestockRepository;
import com.firomsa.inventory.repository.UserRepository;
import com.firomsa.inventory.v1.dto.RestockRequestDTO;
import com.firomsa.inventory.v1.dto.RestockResponseDTO;
import com.firomsa.inventory.v1.mapper.RestockMapper;

@ExtendWith(MockitoExtension.class)
public class RestockServiceUnitTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestockMapper restockMapper;

    @Mock
    private RestockRepository restockRepository;

    @InjectMocks
    private RestockService restockService;

    Product getProduct() {
        return Product.builder().id(UUID.randomUUID()).name("Test Product").quantity(10).build();
    }

    User getUser() {
        return User.builder().id(UUID.randomUUID()).email("user@example.com").build();
    }

    @Test
    void shouldCreateARestock() {
        var productId = UUID.randomUUID();
        var request = RestockRequestDTO.builder().productId(productId).quantity(5).build();
        var response = RestockResponseDTO.builder().productId(request.getProductId())
                .quantity(request.getQuantity()).message("Restock recorded successfully").build();
        var product = Product.builder().id(productId).name("Test Product").quantity(10).build();
        var user = getUser();
        var restock = Restock.builder().product(product).quantity(request.getQuantity())
                .restockedBy(user).build();

        when(restockMapper.toDTO(any())).thenReturn(response);
        when(restockMapper.toModel(request)).thenReturn(restock);
        when(restockRepository.save(any())).thenReturn(restock);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        var result = restockService.createRestock(request, "user@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("Restock recorded successfully");
        assertThat(result.getQuantity()).isEqualTo(request.getQuantity());
        assertThat(result.getProductId()).isEqualTo(request.getProductId());
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        var request = RestockRequestDTO.builder().productId(UUID.randomUUID()).quantity(5).build();
        when(productRepository.findById(request.getProductId())).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> {
            restockService.createRestock(request, "user@example.com");
        });
        assertThat(exception.getMessage()).contains("Product not found with id: " + request.getProductId());
    }

    @Test
    void shouldReturnAllRestocks() {
        var product = getProduct();
        var user = getUser();
        var firstRestock = Restock.builder().id(UUID.randomUUID()).product(product).restockedBy(user)
                .quantity(2).build();
        var secondRestock = Restock.builder().id(UUID.randomUUID()).product(product).restockedBy(user)
                .quantity(4).build();

        var firstResponse = RestockResponseDTO.builder().productId(product.getId()).quantity(2)
                .build();
        var secondResponse = RestockResponseDTO.builder().productId(product.getId()).quantity(4)
                .build();

        when(restockRepository.findAll()).thenReturn(List.of(firstRestock, secondRestock));
        when(restockMapper.toDTO(eq(firstRestock))).thenReturn(firstResponse);
        when(restockMapper.toDTO(eq(secondRestock))).thenReturn(secondResponse);

        var result = restockService.getAllRestocks();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getQuantity()).isEqualTo(2);
        assertThat(result.get(1).getQuantity()).isEqualTo(4);
    }

    @Test
    void shouldReturnRestockById() {
        var restockId = UUID.randomUUID();
        var product = getProduct();
        var user = getUser();
        var restock = Restock.builder().id(restockId).product(product).restockedBy(user).quantity(3)
                .build();
        var response = RestockResponseDTO.builder().productId(product.getId()).quantity(3).build();

        when(restockRepository.findById(restockId)).thenReturn(Optional.of(restock));
        when(restockMapper.toDTO(restock)).thenReturn(response);

        var result = restockService.getRestockById(restockId);

        assertThat(result.getQuantity()).isEqualTo(3);
        assertThat(result.getProductId()).isEqualTo(product.getId());
    }

    @Test
    void shouldThrowExceptionWhenRestockNotFoundById() {
        var restockId = UUID.randomUUID();
        when(restockRepository.findById(restockId)).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> {
            restockService.getRestockById(restockId);
        });
        assertThat(exception.getMessage()).contains("Restock not found with id: " + restockId);
    }

    @Test
    void shouldReturnRestocksByEmployeeEmail() {
        var email = "employee@example.com";
        var product = getProduct();
        var user = getUser();
        var restock = Restock.builder().id(UUID.randomUUID()).product(product).restockedBy(user)
                .quantity(6).build();
        var response = RestockResponseDTO.builder().productId(product.getId()).quantity(6).build();

        when(restockRepository.findByRestockedByEmail(email)).thenReturn(List.of(restock));
        when(restockMapper.toDTO(restock)).thenReturn(response);

        var result = restockService.getRestocksByEmployee(email);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuantity()).isEqualTo(6);
    }

    @Test
    void shouldDeleteRestockById() {
        var restockId = UUID.randomUUID();
        var restock = Restock.builder().id(restockId).build();
        when(restockRepository.findById(restockId)).thenReturn(Optional.of(restock));
        doNothing().when(restockRepository).delete(restock);

        restockService.deleteRestockById(restockId);

        verify(restockRepository).delete(restock);
    }

    @Test
    void shouldThrowExceptionWhenDeletingUnknownRestock() {
        var restockId = UUID.randomUUID();
        when(restockRepository.findById(restockId)).thenReturn(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> {
            restockService.deleteRestockById(restockId);
        });

        assertThat(exception.getMessage()).contains("Restock not found with id: " + restockId);
    }
}
