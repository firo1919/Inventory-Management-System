package com.firomsa.inventory.v1.controller.e2eTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

public class EmployeeControllerE2ETest extends AbstractE2ETest {

    private static final String EMPLOYEE_BASE_URL = "/api/v1/employee";
    private static final String SALES_BASE_URL = "/api/v1/sales";
    private static final String RESTOCKS_BASE_URL = "/api/v1/restocks";

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

    private String createSalePayload(UUID productId, int quantity, BigDecimal salePrice) {
        return "{\"productId\":\"" + productId + "\",\"quantity\":" + quantity
                + ",\"salePrice\":" + salePrice + "}";
    }

    private String createRestockPayload(UUID productId, int quantity) {
        return "{\"productId\":\"" + productId + "\",\"quantity\":" + quantity + "}";
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
    void shouldReturnUnauthorizedForEmployeeBaseWhenMissingToken() {
        var response = client.get().uri(EMPLOYEE_BASE_URL + "/sales").exchange();
        response.expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnForbiddenForEmployeeBaseWhenUsingAdminToken() {
        registerAndConfirmAdminIfNeeded();

        var response = client.get().uri(EMPLOYEE_BASE_URL + "/sales")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(adminAccessToken))
                .exchange();

        response.expectStatus().isForbidden();
    }

    @Test
    void shouldReturnOnlyOwnSalesForAuthenticatedEmployee() {
        registerAndConfirmAdminIfNeeded();
        UUID categoryId = createCategory(adminAccessToken, randomSuffix());

        String firstSuffix = randomSuffix();
        String secondSuffix = randomSuffix();
        UUID firstProductId = createProduct(adminAccessToken, firstSuffix, categoryId);
        UUID secondProductId = createProduct(adminAccessToken, secondSuffix, categoryId);

        String firstEmployeeToken = registerAndLoginEmployee(adminAccessToken, randomSuffix());
        String secondEmployeeToken = registerAndLoginEmployee(adminAccessToken, randomSuffix());

        var firstSaleResponse = client.post().uri(SALES_BASE_URL)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(firstEmployeeToken))
                .contentType(APPLICATION_JSON).body(createSalePayload(firstProductId, 2, BigDecimal.valueOf(12.0)))
                .exchange();
        firstSaleResponse.expectStatus().isOk();

        var secondSaleResponse = client.post().uri(SALES_BASE_URL)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(secondEmployeeToken))
                .contentType(APPLICATION_JSON).body(createSalePayload(secondProductId, 4, BigDecimal.valueOf(13.0)))
                .exchange();
        secondSaleResponse.expectStatus().isOk();

        var response = client.get().uri(EMPLOYEE_BASE_URL + "/sales")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(firstEmployeeToken))
                .exchange();

        response.expectStatus().isOk();
        String body = response.returnResult(String.class).getResponseBody();
        assertThat(body).contains(firstProductId.toString());
        assertThat(body).doesNotContain(secondProductId.toString());
    }

    @Test
    void shouldReturnOnlyOwnRestocksForAuthenticatedEmployee() {
        registerAndConfirmAdminIfNeeded();
        UUID categoryId = createCategory(adminAccessToken, randomSuffix());

        String firstSuffix = randomSuffix();
        String secondSuffix = randomSuffix();
        UUID firstProductId = createProduct(adminAccessToken, firstSuffix, categoryId);
        UUID secondProductId = createProduct(adminAccessToken, secondSuffix, categoryId);

        String firstEmployeeToken = registerAndLoginEmployee(adminAccessToken, randomSuffix());
        String secondEmployeeToken = registerAndLoginEmployee(adminAccessToken, randomSuffix());

        var firstRestockResponse = client.post().uri(RESTOCKS_BASE_URL)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(firstEmployeeToken))
                .contentType(APPLICATION_JSON).body(createRestockPayload(firstProductId, 6))
                .exchange();
        firstRestockResponse.expectStatus().isOk();

        var secondRestockResponse = client.post().uri(RESTOCKS_BASE_URL)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(secondEmployeeToken))
                .contentType(APPLICATION_JSON).body(createRestockPayload(secondProductId, 9))
                .exchange();
        secondRestockResponse.expectStatus().isOk();

        var response = client.get().uri(EMPLOYEE_BASE_URL + "/restocks")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(firstEmployeeToken))
                .exchange();

        response.expectStatus().isOk();
        String body = response.returnResult(String.class).getResponseBody();
        assertThat(body).contains(firstProductId.toString());
        assertThat(body).doesNotContain(secondProductId.toString());
    }
}
