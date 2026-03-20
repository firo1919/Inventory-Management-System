package com.firomsa.inventory.v1.controller.unitTest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
import com.firomsa.inventory.v1.controller.CategoryController;
import com.firomsa.inventory.v1.dto.CategoryResponseDTO;
import com.firomsa.inventory.v1.service.CategoryService;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CategoryControllerUnitTest {

    @MockitoBean
    private CategoryService categoryService;

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE_URL = "/api/v1/categories";

    private CategoryResponseDTO sampleCategory(UUID id, String name) {
        return CategoryResponseDTO.builder().id(id).name(name)
                .createdAt(LocalDateTime.of(2026, 3, 19, 8, 0))
                .updatedAt(LocalDateTime.of(2026, 3, 19, 9, 0)).productIds(Set.of()).build();
    }

    @Test
    void shouldGetAllCategories() throws Exception {
        CategoryResponseDTO first = sampleCategory(UUID.randomUUID(), "Beverages");
        CategoryResponseDTO second = sampleCategory(UUID.randomUUID(), "Snacks");
        when(categoryService.getAll()).thenReturn(List.of(first, second));

        mockMvc.perform(get(BASE_URL)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Beverages"))
                .andExpect(jsonPath("$[1].name").value("Snacks"));

        verify(categoryService).getAll();
    }

    @Test
    void shouldGetCategoryById() throws Exception {
        UUID categoryId = UUID.randomUUID();
        CategoryResponseDTO response = sampleCategory(categoryId, "Beverages");
        when(categoryService.getById(categoryId)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/{id}", categoryId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Beverages"));

        verify(categoryService).getById(categoryId);
    }
}
