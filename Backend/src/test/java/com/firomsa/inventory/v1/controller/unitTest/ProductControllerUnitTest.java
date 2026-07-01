package com.firomsa.inventory.v1.controller.unitTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.firomsa.inventory.support.TestCacheConfig;
import com.firomsa.inventory.v1.controller.ProductController;
import com.firomsa.inventory.v1.dto.PageResponse;
import com.firomsa.inventory.v1.dto.ProductResponseDTO;
import com.firomsa.inventory.v1.service.ProductService;

@WebMvcTest(ProductController.class)
@Import(TestCacheConfig.class)
@AutoConfigureMockMvc
@WithMockUser
public class ProductControllerUnitTest {

    @MockitoBean
    private ProductService productService;

    @Autowired
    private MockMvcTester mockMvc;

    private static final String BASE_URL = "/api/v1/products";

    private ProductResponseDTO sampleProduct(UUID id, String name) {
        return ProductResponseDTO.builder().id(id).name(name).sku("SKU-001")
                .description("Sample product").sellingPrice(new BigDecimal("100.00"))
                .costPrice(new BigDecimal("80.00")).quantity(20).lowStockThreshold(2).active(true)
                .createdAt(LocalDateTime.of(2026, 3, 19, 10, 0).toString())
                .updatedAt(LocalDateTime.of(2026, 3, 19, 10, 30).toString()).categoryIds(Set.of())
                .imageUrls(List.of("https://cdn.example.com/image-1.jpg")).build();
    }

    @Test
    void shouldGetAllProducts() {
        ProductResponseDTO first = sampleProduct(UUID.randomUUID(), "Coffee");
        ProductResponseDTO second = sampleProduct(UUID.randomUUID(), "Tea");
        PageResponse<ProductResponseDTO> page = PageResponse.<ProductResponseDTO>builder()
                .content(List.of(first, second)).pageNumber(0).pageSize(10)
                .totalElements(2).totalPages(1).first(true).last(true).empty(false).build();
        when(productService.getAll(any(Pageable.class))).thenReturn(page);

        MvcTestResult result = mockMvc.get().uri(BASE_URL).exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.content[0].name").asString().isEqualTo("Coffee");
        assertThat(result).bodyJson().extractingPath("$.content[1].name").asString().isEqualTo("Tea");
        verify(productService).getAll(any(Pageable.class));
    }

    @Test
    void shouldGetProductById() {
        UUID productId = UUID.randomUUID();
        ProductResponseDTO response = sampleProduct(productId, "Coffee");
        when(productService.getById(productId)).thenReturn(response);

        MvcTestResult result = mockMvc.get().uri(BASE_URL + "/{id}", productId).exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.id").asString()
                .isEqualTo(productId.toString());
        assertThat(result).bodyJson().extractingPath("$.name").asString().isEqualTo("Coffee");
        verify(productService).getById(productId);
    }
}
