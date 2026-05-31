package com.firomsa.inventory.v1.controller.integrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.firomsa.inventory.repository.CategoryRepository;

public class CategoryControllerIntTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    private static final String BASE_URL = "/api/v1/categories";

    @AfterEach
    void tearDown() {
        categoryRepository.deleteAll();
    }

    private String randomSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private void createCategoryAsAdmin(String suffix) {
        MvcTestResult response = mockMvc.post().uri("/api/v1/admin/categories")
                .contentType(APPLICATION_JSON)
                .content("{\"name\":\"Category " + suffix + "\"}").exchange();

        assertThat(response).hasStatusOk();
    }

    private UUID findCategoryIdBySuffix(String suffix) {
        return categoryRepository.findAll().stream()
                .filter(c -> ("Category " + suffix).equals(c.getName()))
                .findFirst()
                .orElseThrow()
                .getId();
    }

    @Test
    void shouldRejectUnauthorizedGetAllCategories() {
        assertThat(mockMvc.get().uri(BASE_URL).exchange()).hasStatus(401);
    }

    @Test
    void shouldRejectUnauthorizedGetCategoryById() {
        assertThat(mockMvc.get().uri(BASE_URL + "/{id}", UUID.randomUUID()).exchange())
                .hasStatus(401);
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldGetAllCategoriesWhenAuthenticated() {
        String suffix = randomSuffix();
        createCategoryAsAdmin(suffix);

        MvcTestResult response = mockMvc.get().uri(BASE_URL).exchange();
        assertThat(response).hasStatusOk();
        assertThat(response).bodyText().contains("Category " + suffix);
    }

    @Test
    @WithMockUser
    void shouldReturnEmptyCategoryListWhenAuthenticatedAndNoCategoryExists() {
        MvcTestResult response = mockMvc.get().uri(BASE_URL).exchange();

        assertThat(response).hasStatusOk();
        assertThat(response).bodyText().contains("[]");
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldGetCategoryByIdWhenAuthenticated() {
        String suffix = randomSuffix();
        createCategoryAsAdmin(suffix);
        UUID categoryId = findCategoryIdBySuffix(suffix);

        MvcTestResult response = mockMvc.get().uri(BASE_URL + "/{id}", categoryId).exchange();

        assertThat(response).hasStatusOk();
        assertThat(response).bodyJson().extractingPath("$.id").asString()
                .isEqualTo(categoryId.toString());
        assertThat(response).bodyJson().extractingPath("$.name").asString()
                .isEqualTo("Category " + suffix);
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestWhenCategoryIdIsMalformed() {
        assertThat(mockMvc.get().uri(BASE_URL + "/not-a-uuid").exchange()).hasStatus(400);
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenCategoryDoesNotExist() {
        assertThat(mockMvc.get().uri(BASE_URL + "/{id}", UUID.randomUUID()).exchange())
                .hasStatus(404);
    }
}
