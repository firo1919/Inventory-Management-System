package com.firomsa.inventory.v1.controller.unitTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.firomsa.inventory.v1.controller.CategoryController;
import com.firomsa.inventory.v1.dto.CategoryResponseDTO;
import com.firomsa.inventory.v1.service.CategoryService;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc
@WithMockUser
public class CategoryControllerUnitTest {

    @MockitoBean
    private CategoryService categoryService;

    @Autowired
    private MockMvcTester mockMvc;

    private static final String BASE_URL = "/api/v1/categories";

    private CategoryResponseDTO sampleCategory(UUID id, String name) {
        return CategoryResponseDTO.builder().id(id).name(name)
                .createdAt(LocalDateTime.of(2026, 3, 19, 8, 0))
                .updatedAt(LocalDateTime.of(2026, 3, 19, 9, 0)).productIds(Set.of()).build();
    }

    @Test
    void shouldGetAllCategories() {
        CategoryResponseDTO first = sampleCategory(UUID.randomUUID(), "Beverages");
        CategoryResponseDTO second = sampleCategory(UUID.randomUUID(), "Snacks");
        when(categoryService.getAll()).thenReturn(List.of(first, second));

        MvcTestResult result = mockMvc.get().uri(BASE_URL).exchange();
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$[0].name").asString().isEqualTo("Beverages");
        assertThat(result).bodyJson().extractingPath("$[1].name").asString().isEqualTo("Snacks");

        verify(categoryService).getAll();
    }

    @Test
    void shouldGetCategoryById() {
        UUID categoryId = UUID.randomUUID();
        CategoryResponseDTO response = sampleCategory(categoryId, "Beverages");
        when(categoryService.getById(categoryId)).thenReturn(response);

        MvcTestResult result = mockMvc.get().uri(BASE_URL + "/{id}", categoryId).exchange();
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.id").asString()
                .isEqualTo(categoryId.toString());
        assertThat(result).bodyJson().extractingPath("$.name").asString().isEqualTo("Beverages");

        verify(categoryService).getById(categoryId);
    }
}
