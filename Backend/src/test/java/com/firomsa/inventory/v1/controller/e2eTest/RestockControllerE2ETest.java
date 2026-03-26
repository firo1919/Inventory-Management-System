package com.firomsa.inventory.v1.controller.e2eTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

public class RestockControllerE2ETest extends AbstractE2ETest {

    private static final String RESTOCKS_BASE_URL = "/api/v1/restocks";

    private static String adminAccessToken;

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

    private String createRestockPayload(UUID productId, Integer quantity) {
        return "{\"productId\":\"" + productId + "\",\"quantity\":" + quantity + "}";
    }

    @Test
    void shouldReturnUnauthorizedWhenMissingToken() {
        var response = client.post().uri(RESTOCKS_BASE_URL).contentType(APPLICATION_JSON)
                .body("{\"quantity\":1,\"productId\":\"" + UUID.randomUUID() + "\"}")
                .exchange();

        response.expectStatus().isUnauthorized();
    }

    @Test
    void shouldCreateRestockForAuthenticatedAdmin() {
        registerAndConfirmAdminIfNeeded();
        UUID categoryId = createCategory(adminAccessToken, randomSuffix());
        UUID productId = createProduct(adminAccessToken, randomSuffix(), categoryId);
        String payload = createRestockPayload(productId, 5);

        var response = client.post().uri(RESTOCKS_BASE_URL)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(adminAccessToken))
                .contentType(APPLICATION_JSON).body(payload).exchange();

        response.expectStatus().isOk();
        String body = response.returnResult(String.class).getResponseBody();
        assertThat(body).contains("Restock recorded successfully");
        assertThat(body).contains("\"quantity\":5");
        assertThat(body).contains(productId.toString());
    }

    @Test
    void shouldCreateRestockForAuthenticatedEmployee() {
        registerAndConfirmAdminIfNeeded();
        UUID categoryId = createCategory(adminAccessToken, randomSuffix());
        UUID productId = createProduct(adminAccessToken, randomSuffix(), categoryId);
        String employeeToken = registerAndLoginEmployee(adminAccessToken, randomSuffix());

        var response = client.post().uri(RESTOCKS_BASE_URL)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(employeeToken))
                .contentType(APPLICATION_JSON).body(createRestockPayload(productId, 3)).exchange();

        response.expectStatus().isOk();
        String body = response.returnResult(String.class).getResponseBody();
        assertThat(body).contains("Restock recorded successfully");
        assertThat(body).contains("\"quantity\":3");
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() {
        registerAndConfirmAdminIfNeeded();

        var response = client.post().uri(RESTOCKS_BASE_URL)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(adminAccessToken))
                .contentType(APPLICATION_JSON)
                .body(createRestockPayload(UUID.randomUUID(), 5)).exchange();

        response.expectStatus().isNotFound();
        String body = response.returnResult(String.class).getResponseBody();
        assertThat(body).contains("Product not found with id");
    }

    @Test
    void shouldReturnBadRequestWhenPayloadIsInvalid() {
        registerAndConfirmAdminIfNeeded();
        UUID categoryId = createCategory(adminAccessToken, randomSuffix());
        UUID productId = createProduct(adminAccessToken, randomSuffix(), categoryId);

        var response = client.post().uri(RESTOCKS_BASE_URL)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(adminAccessToken))
                .contentType(APPLICATION_JSON)
                .body("{\"productId\":\"" + productId + "\"}")
                .exchange();

        response.expectStatus().isBadRequest();
        String body = response.returnResult(String.class).getResponseBody();
        assertThat(body).contains("Validation failed for fields");
        assertThat(body).contains("quantity");
    }

    @Test
    void shouldGetRestockByIdForAuthenticatedEmployee() {
        registerAndConfirmAdminIfNeeded();
        UUID categoryId = createCategory(adminAccessToken, randomSuffix());
        UUID productId = createProduct(adminAccessToken, randomSuffix(), categoryId);
        String employeeToken = registerAndLoginEmployee(adminAccessToken, randomSuffix());

        var createRestockResponse = client.post().uri(RESTOCKS_BASE_URL)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(employeeToken))
                .contentType(APPLICATION_JSON).body(createRestockPayload(productId, 7))
                .exchange();
        createRestockResponse.expectStatus().isOk();

        UUID restockId = restockRepository.findAll().stream()
                .filter(restock -> restock.getProduct() != null
                        && productId.equals(restock.getProduct().getId()))
                .map(restock -> restock.getId()).reduce((first, second) -> second).orElseThrow();

        var getResponse = client.get().uri(RESTOCKS_BASE_URL + "/" + restockId)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(employeeToken)).exchange();

        getResponse.expectStatus().isOk();
        String body = getResponse.returnResult(String.class).getResponseBody();
        assertThat(body).contains(productId.toString());
        assertThat(body).contains("\"quantity\":7");
    }

    @Test
    void shouldReturnNotFoundWhenRestockIdDoesNotExist() {
        registerAndConfirmAdminIfNeeded();

        var response = client.get().uri(RESTOCKS_BASE_URL + "/" + UUID.randomUUID())
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(adminAccessToken))
                .exchange();

        response.expectStatus().isNotFound();
        String body = response.returnResult(String.class).getResponseBody();
        assertThat(body).contains("Restock not found with id");
    }
}
