package com.firomsa.inventory.v1.controller.e2eTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

public class ProductControllerE2ETest extends AbstractE2ETest {

    private static final String PRODUCT_BASE_URL = "/api/v1/products";

    private String adminAccessToken;

    private String createCategoryPayload(String suffix) {
        return "{\"name\":\"Category-" + suffix + "\"}";
    }

    private String createProductPayload(String suffix, UUID categoryId) {
        return """
                {
                    "name": "Product-%s",
                    "sku": "SKU-%s",
                    "description": "E2E product",
                    "sellingPrice": 120.50,
                    "costPrice": 90.00,
                    "quantity": 25,
                    "lowStockThreshold": 5,
                    "categoryIds": ["%s"]
                }
                """.formatted(suffix, suffix, categoryId);
    }

    private void registerAndConfirmAdminIfNeeded() {
        if (adminAccessToken != null) {
            return;
        }

        String suffix = randomSuffix();
        String email = adminEmailForSuffix(suffix);

        var registerResponse = client.post().uri(AUTH_BASE_URL + "/admins")
                .contentType(APPLICATION_JSON).body(registerAdminPayload(suffix)).exchange();
        registerResponse.expectStatus().isOk();

        String otp = latestOtpForEmail(email);
        String confirmPayload = "{\"otp\":\"" + otp + "\",\"email\":\"" + email + "\"}";
        var confirmResponse = client.post().uri(AUTH_BASE_URL + "/confirm-otp")
                .contentType(APPLICATION_JSON).body(confirmPayload).exchange();
        confirmResponse.expectStatus().isOk();

        adminAccessToken = loginByEmail(email);
    }

    private UUID createCategory(String accessToken, String suffix) {
        var response = client.post().uri(ADMIN_BASE_URL + "/categories")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                .contentType(APPLICATION_JSON).body(createCategoryPayload(suffix)).exchange();
        response.expectStatus().isOk();

        String categoryName = "Category-" + suffix;
        return categoryRepository.findAll().stream().filter(c -> categoryName.equals(c.getName()))
                .map(c -> c.getId()).findFirst().orElseThrow();
    }

    private UUID createProduct(String accessToken, String suffix, UUID categoryId) {
        var response = client.post().uri(ADMIN_BASE_URL + "/products")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                .contentType(APPLICATION_JSON).body(createProductPayload(suffix, categoryId))
                .exchange();
        response.expectStatus().isOk();

        String sku = "SKU-" + suffix;
        return productRepository.findAll().stream().filter(p -> sku.equals(p.getSku()))
                .map(p -> p.getId()).findFirst().orElseThrow();
    }

    private String registerAndLoginEmployee(String adminToken, String suffix) {
        var registerEmployeeResponse = client.post().uri(ADMIN_BASE_URL + "/employees")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(adminToken))
                .contentType(APPLICATION_JSON).body(registerEmployeePayload(suffix)).exchange();
        registerEmployeeResponse.expectStatus().isOk();

        String employeeEmail = employeeEmailForSuffix(suffix);
        String otp = latestOtpForEmail(employeeEmail);
        String confirmPayload = "{\"otp\":\"" + otp + "\",\"email\":\"" + employeeEmail + "\"}";
        var confirmResponse = client.post().uri(AUTH_BASE_URL + "/confirm-otp")
                .contentType(APPLICATION_JSON).body(confirmPayload).exchange();
        confirmResponse.expectStatus().isOk();

        return loginByEmail(employeeEmail);
    }

    @Test
    void shouldReturnUnauthorizedWhenMissingToken() {
        var response = client.get().uri(PRODUCT_BASE_URL).exchange();
        response.expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnAllProductsForAuthenticatedUser() {
        registerAndConfirmAdminIfNeeded();
        UUID categoryId = createCategory(adminAccessToken, randomSuffix());
        String suffix = randomSuffix();
        createProduct(adminAccessToken, suffix, categoryId);

        var response = client.get().uri(PRODUCT_BASE_URL)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(adminAccessToken))
                .exchange();

        response.expectStatus().isOk();
        String body = response.returnResult(String.class).getResponseBody();
        assertThat(body).contains("SKU-" + suffix);
    }

    @Test
    void shouldReturnProductByIdForAuthenticatedUser() {
        registerAndConfirmAdminIfNeeded();
        UUID categoryId = createCategory(adminAccessToken, randomSuffix());
        String suffix = randomSuffix();
        UUID productId = createProduct(adminAccessToken, suffix, categoryId);

        var response = client.get().uri(PRODUCT_BASE_URL + "/" + productId)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(adminAccessToken))
                .exchange();

        response.expectStatus().isOk();
        String body = response.returnResult(String.class).getResponseBody();
        assertThat(body).contains("SKU-" + suffix);
    }

    @Test
    void shouldReturnNotFoundForUnknownProductId() {
        registerAndConfirmAdminIfNeeded();

        var response = client.get().uri(PRODUCT_BASE_URL + "/" + UUID.randomUUID())
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(adminAccessToken))
                .exchange();

        response.expectStatus().isNotFound();
    }

    @Test
    void shouldAllowEmployeeToReadProducts() {
        registerAndConfirmAdminIfNeeded();
        UUID categoryId = createCategory(adminAccessToken, randomSuffix());
        createProduct(adminAccessToken, randomSuffix(), categoryId);
        String employeeToken = registerAndLoginEmployee(adminAccessToken, randomSuffix());

        var response = client.get().uri(PRODUCT_BASE_URL)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(employeeToken)).exchange();

        response.expectStatus().isOk();
    }
}
