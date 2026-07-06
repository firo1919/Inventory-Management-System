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
import com.firomsa.inventory.repository.ProductRepository;

public class ProductControllerIntTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private static final String BASE_URL = "/api/v1/products";

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    private String randomSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private UUID createCategoryAsAdmin(String suffix) {
        MvcTestResult response = mockMvc.post().uri("/api/v1/admin/categories").contentType(APPLICATION_JSON)
                .content("{\"name\":\"Category " + suffix + "\"}").exchange();

        assertThat(response).hasStatus(org.springframework.http.HttpStatus.CREATED);
        return categoryRepository.findAll().stream()
                .filter(c -> ("Category " + suffix).equals(c.getName())).findFirst().orElseThrow()
                .getId();
    }

    private void createProductAsAdmin(String suffix) {
        UUID categoryId = createCategoryAsAdmin(suffix);

        MvcTestResult response = mockMvc.post().uri("/api/v1/admin/products")
                .contentType(APPLICATION_JSON).content("""
                        {
                            "name": "Wireless Mouse %s",
                            "sku": "SKU-WM-%s",
                            "description": "Ergonomic mouse",
                            "sellingPrice": 49.99,
                            "costPrice": 25.00,
                            "quantity": 15,
                            "lowStockThreshold": 3,
                            "categoryIds": ["%s"]
                        }
                        """.formatted(suffix, suffix, categoryId)).exchange();

        assertThat(response).hasStatus(org.springframework.http.HttpStatus.CREATED);
    }

    private UUID findProductIdBySuffix(String suffix) {
        return productRepository.findAll().stream()
                .filter(p -> ("Wireless Mouse " + suffix).equals(p.getName())).findFirst()
                .orElseThrow().getId();
    }

    @Test
    void shouldRejectUnauthorizedGetAllProducts() {
        assertThat(mockMvc.get().uri(BASE_URL).exchange()).hasStatus(401);
    }

    @Test
    void shouldRejectUnauthorizedGetProductById() {
        assertThat(mockMvc.get().uri(BASE_URL + "/{id}", UUID.randomUUID()).exchange())
                .hasStatus(401);
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldGetAllProductsWhenAuthenticated() {
        String suffix = randomSuffix();
        createProductAsAdmin(suffix);

        MvcTestResult response = mockMvc.get().uri(BASE_URL).exchange();

        assertThat(response).hasStatusOk();
        assertThat(response).bodyText().contains("Wireless Mouse " + suffix);
    }

    @Test
    @WithMockUser
    void shouldReturnEmptyProductListWhenAuthenticatedAndNoProductExists() {
        MvcTestResult response = mockMvc.get().uri(BASE_URL).exchange();

        assertThat(response).hasStatusOk();
        assertThat(response).bodyText().contains("[]");
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldGetProductByIdWhenAuthenticated() {
        String suffix = randomSuffix();
        createProductAsAdmin(suffix);
        UUID productId = findProductIdBySuffix(suffix);

        MvcTestResult response = mockMvc.get().uri(BASE_URL + "/{id}", productId).exchange();

        assertThat(response).hasStatusOk();
        assertThat(response).bodyJson().extractingPath("$.id").asString()
                .isEqualTo(productId.toString());
        assertThat(response).bodyJson().extractingPath("$.sku").asString()
                .isEqualTo("SKU-WM-" + suffix);
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestWhenProductIdIsMalformed() {
        assertThat(mockMvc.get().uri(BASE_URL + "/not-a-uuid").exchange()).hasStatus(400);
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenProductDoesNotExist() {
        assertThat(mockMvc.get().uri(BASE_URL + "/{id}", UUID.randomUUID()).exchange())
                .hasStatus(404);
    }

    @Test
    void shouldRejectUnauthorizedLowStockRequest() {
        assertThat(mockMvc.get().uri(BASE_URL + "/low-stock").exchange())
                .hasStatus(401);
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldReturnLowStockProductsWhenAuthenticated() {
        // Create a low stock product
        String suffix = randomSuffix();
        UUID categoryId = createCategoryAsAdmin(suffix);

        // Create product with quantity below threshold
        var lowStockResponse = mockMvc.post().uri("/api/v1/admin/products")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                            "name": "Low Stock Item %s",
                            "sku": "LOW-%s",
                            "description": "Low stock test",
                            "sellingPrice": 49.99,
                            "costPrice": 25.00,
                            "quantity": 2,
                            "lowStockThreshold": 5,
                            "categoryIds": ["%s"]
                        }
                        """.formatted(suffix, suffix, categoryId)).exchange();
        assertThat(lowStockResponse).hasStatus(org.springframework.http.HttpStatus.CREATED);

        // Get low stock products
        MvcTestResult response = mockMvc.get().uri(BASE_URL + "/low-stock").exchange();

        assertThat(response).hasStatusOk();
        assertThat(response).bodyText().contains("Low Stock Item " + suffix);
        assertThat(response).bodyJson().extractingPath("$.content[0].quantity").asNumber().isEqualTo(2);
        assertThat(response).bodyJson().extractingPath("$.content[0].lowStockThreshold").asNumber().isEqualTo(5);
    }

    @Test
    @WithMockUser
    void shouldReturnEmptyListWhenNoLowStockProducts() {
        MvcTestResult response = mockMvc.get().uri(BASE_URL + "/low-stock").exchange();

        assertThat(response).hasStatusOk();
        assertThat(response).bodyText().contains("[]");
    }
}
