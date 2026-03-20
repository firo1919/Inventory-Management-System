package com.firomsa.inventory.v1.controller.unitTest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.firomsa.inventory.v1.controller.ProductController;
import com.firomsa.inventory.v1.dto.ProductResponseDTO;
import com.firomsa.inventory.v1.service.ProductService;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerUnitTest {

    @MockitoBean
    private ProductService productService;

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE_URL = "/api/v1/products";

    private ProductResponseDTO sampleProduct(UUID id, String name) {
        return ProductResponseDTO.builder().id(id).name(name).sku("SKU-001")
                .description("Sample product").sellingPrice(new BigDecimal("100.00"))
                .costPrice(new BigDecimal("80.00")).quantity(20).lowStockThreshold(2).active(true)
                .createdAt(LocalDateTime.of(2026, 3, 19, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 3, 19, 10, 30)).categoryIds(Set.of())
                .imageUrls(List.of("https://cdn.example.com/image-1.jpg")).build();
    }

    @Test
    void shouldGetAllProducts() throws Exception {
        ProductResponseDTO first = sampleProduct(UUID.randomUUID(), "Coffee");
        ProductResponseDTO second = sampleProduct(UUID.randomUUID(), "Tea");
        when(productService.getAll()).thenReturn(List.of(first, second));

        mockMvc.perform(get(BASE_URL)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Coffee"))
                .andExpect(jsonPath("$[1].name").value("Tea"));

        verify(productService).getAll();
    }

    @Test
    void shouldGetProductById() throws Exception {
        UUID productId = UUID.randomUUID();
        ProductResponseDTO response = sampleProduct(productId, "Coffee");
        when(productService.getById(productId)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/{id}", productId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Coffee"));

        verify(productService).getById(productId);
    }
}
