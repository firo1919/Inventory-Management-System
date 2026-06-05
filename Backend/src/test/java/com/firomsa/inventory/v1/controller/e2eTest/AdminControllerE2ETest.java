package com.firomsa.inventory.v1.controller.e2eTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

public class AdminControllerE2ETest extends AbstractE2ETest {

    private String registeredAdminEmail;
    private String registeredAdminAccessToken;

    private UUID randomId() {
        return UUID.randomUUID();
    }

    private String updateEmployeePayload(String suffix) {
        return """
                {
                    "firstName": "UpdatedFirst%s",
                    "lastName": "UpdatedLast%s",
                    "username": "updated_%s",
                    "password": "%s",
                    "email": "updated.employee_%s@example.com",
                    "role": "EMPLOYEE",
                    "phone": "+251922%s"
                }
                """.formatted(suffix, suffix, suffix, DEFAULT_PASSWORD, suffix,
                suffix.substring(0, 6));
    }

    private String createCategoryPayload(String suffix) {
        return """
                {
                    "name": "Category-%s"
                }
                """.formatted(suffix);
    }

    private String updateCategoryPayload(String suffix) {
        return """
                {
                    "name": "Updated-Category-%s"
                }
                """.formatted(suffix);
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

    private String updateProductPayload(String suffix, UUID categoryId) {
        return """
                {
                    "name": "Updated-Product-%s",
                    "description": "Updated description",
                    "sellingPrice": 150.75,
                    "costPrice": 100.20,
                    "quantity": 30,
                    "lowStockThreshold": 7,
                    "categoryIds": ["%s"]
                }
                """.formatted(suffix, categoryId);
    }

    private String updatedEmployeeEmailForSuffix(String suffix) {
        return "updated.employee_" + suffix + "@example.com";
    }

    private void registerAndConfirmAdmin(String suffix) {
        String adminEmail = adminEmailForSuffix(suffix);
        var registerResponse = client.post().uri(AUTH_BASE_URL + "/admins")
                .contentType(APPLICATION_JSON).body(registerAdminPayload(suffix)).exchange();

        registerResponse.expectStatus().isOk();
        String registerBody = registerResponse.returnResult(String.class).getResponseBody();
        assertThat(registerBody)
                .contains("You have successfully registered, confirm the OTP sent to your email");

        String otp = latestOtpForEmail(adminEmail);
        String confirmPayload = "{\"otp\":\"" + otp + "\",\"email\":\"" + adminEmail + "\"}";

        var confirmResponse = client.post().uri(AUTH_BASE_URL + "/confirm-otp")
                .contentType(APPLICATION_JSON).body(confirmPayload).exchange();

        confirmResponse.expectStatus().isOk();
        String confirmBody = confirmResponse.returnResult(String.class).getResponseBody();
        assertThat(confirmBody)
                .contains("Successfully confirmed OTP, please login using your email and password");
        registeredAdminEmail = adminEmail;
        registeredAdminAccessToken = loginByEmail(adminEmail);
    }

    private void confirmOtpForEmail(String email) {
        String otp = latestOtpForEmail(email);
        String confirmPayload = "{\"otp\":\"" + otp + "\",\"email\":\"" + email + "\"}";

        var confirmResponse = client.post().uri(AUTH_BASE_URL + "/confirm-otp")
                .contentType(APPLICATION_JSON).body(confirmPayload).exchange();

        confirmResponse.expectStatus().isOk();
        String confirmBody = confirmResponse.returnResult(String.class).getResponseBody();
        assertThat(confirmBody)
                .contains("Successfully confirmed OTP, please login using your email and password");
    }

    private void registerEmployee(String accessToken, String suffix) {
        var employeeResponse = client.post().uri(ADMIN_BASE_URL + "/employees").contentType(APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                .body(registerEmployeePayload(suffix)).exchange();

        employeeResponse.expectStatus().isOk();
        String employeeBody = employeeResponse.returnResult(String.class).getResponseBody();
        assertThat(employeeBody).contains(employeeEmailForSuffix(suffix));
    }

    private UUID employeeIdByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow().getId();
    }

    private UUID categoryIdByName(String name) {
        return categoryRepository.findAll().stream()
                .filter(category -> name.equals(category.getName()))
                .map(category -> category.getId()).findFirst()
                .orElseThrow(() -> new IllegalStateException("Category not found: " + name));
    }

    private UUID productIdBySku(String sku) {
        return productRepository.findAll().stream().filter(product -> sku.equals(product.getSku()))
                .map(product -> product.getId()).findFirst()
                .orElseThrow(() -> new IllegalStateException("Product not found with SKU: " + sku));
    }

    private String createCategoryAndGetId(String accessToken, String suffix) {
        String categoryName = "Category-" + suffix;

        var response = client.post().uri(ADMIN_BASE_URL + "/categories")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                .contentType(APPLICATION_JSON).body(createCategoryPayload(suffix)).exchange();

        response.expectStatus().isOk();
        String responseBody = response.returnResult(String.class).getResponseBody();
        assertThat(responseBody).contains(categoryName);
        return categoryIdByName(categoryName).toString();
    }

    private void withAuthenticatedAdmin(Consumer<String> testBody) {
        if (registeredAdminEmail == null) {
            registerAndConfirmAdmin(randomSuffix());
        }
        testBody.accept(registeredAdminAccessToken);
    }

    @Test
    void shouldReturnAllEmployees() {
        String employeeSuffixOne = randomSuffix();
        String employeeSuffixTwo = randomSuffix();

        withAuthenticatedAdmin(accessToken -> {
            registerEmployee(accessToken, employeeSuffixOne);
            registerEmployee(accessToken, employeeSuffixTwo);

            var response = client.get().uri(ADMIN_BASE_URL + "/employees")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken)).exchange();

            response.expectStatus().isOk();
            String responseBody = response.returnResult(String.class).getResponseBody();
            assertThat(responseBody).contains(employeeEmailForSuffix(employeeSuffixOne));
            assertThat(responseBody).contains(employeeEmailForSuffix(employeeSuffixTwo));
        });
    }

    @Test
    void shouldReturnUnauthorizedWhenMissingTokenForAdminEndpoint() {
        var response = client.get().uri(ADMIN_BASE_URL + "/employees").exchange();

        response.expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnForbiddenWhenEmployeeAccessesAdminEndpoint() {
        String employeeSuffix = randomSuffix();

        withAuthenticatedAdmin(adminAccessToken -> {
            registerEmployee(adminAccessToken, employeeSuffix);
            String employeeEmail = employeeEmailForSuffix(employeeSuffix);
            confirmOtpForEmail(employeeEmail);
            String employeeAccessToken = loginByEmail(employeeEmail);

            var response = client.get().uri(ADMIN_BASE_URL + "/employees")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(employeeAccessToken))
                    .exchange();

            response.expectStatus().isForbidden();
        });
    }

    @Test
    void shouldGetEmployeeById() {
        String employeeSuffix = randomSuffix();

        withAuthenticatedAdmin(accessToken -> {
            registerEmployee(accessToken, employeeSuffix);
            String email = employeeEmailForSuffix(employeeSuffix);
            UUID employeeId = employeeIdByEmail(email);

            var response = client.get().uri(ADMIN_BASE_URL + "/employees/" + employeeId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken)).exchange();

            response.expectStatus().isOk();
            String body = response.returnResult(String.class).getResponseBody();
            assertThat(body).contains(email);
        });
    }

    @Test
    void shouldReturnNotFoundWhenEmployeeIdDoesNotExist() {
        withAuthenticatedAdmin(accessToken -> {
            var response = client.get().uri(ADMIN_BASE_URL + "/employees/" + randomId())
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken)).exchange();

            response.expectStatus().isNotFound();
        });
    }

    @Test
    void shouldReturnBadRequestWhenRegisterEmployeePayloadIsInvalid() {
        withAuthenticatedAdmin(accessToken -> {
            String invalidPayload = """
                    {
                        "firstName": "",
                        "lastName": "",
                        "username": "",
                        "password": "short",
                        "email": "not-an-email",
                        "role": "EMPLOYEE",
                        "phone": ""
                    }
                    """;

            var response = client.post().uri(ADMIN_BASE_URL + "/employees")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .contentType(APPLICATION_JSON).body(invalidPayload).exchange();

            response.expectStatus().isBadRequest();
        });
    }

    @Test
    void shouldUpdateEmployee() {
        String employeeSuffix = randomSuffix();
        String updateSuffix = randomSuffix();

        withAuthenticatedAdmin(accessToken -> {
            registerEmployee(accessToken, employeeSuffix);
            UUID employeeId = employeeIdByEmail(employeeEmailForSuffix(employeeSuffix));

            var response = client.put().uri(ADMIN_BASE_URL + "/employees/" + employeeId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .contentType(APPLICATION_JSON).body(updateEmployeePayload(updateSuffix))
                    .exchange();

            response.expectStatus().isOk();
            String body = response.returnResult(String.class).getResponseBody();
            assertThat(body).contains(updatedEmployeeEmailForSuffix(updateSuffix));
        });
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingUnknownEmployee() {
        withAuthenticatedAdmin(accessToken -> {
            var response = client.put().uri(ADMIN_BASE_URL + "/employees/" + randomId())
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .contentType(APPLICATION_JSON).body(updateEmployeePayload(randomSuffix()))
                    .exchange();

            response.expectStatus().isNotFound();
        });
    }

    @Test
    void shouldDeactivateAndActivateEmployee() {
        String employeeSuffix = randomSuffix();

        withAuthenticatedAdmin(accessToken -> {
            registerEmployee(accessToken, employeeSuffix);
            UUID employeeId = employeeIdByEmail(employeeEmailForSuffix(employeeSuffix));

            var deactivateResponse = client.post()
                    .uri(ADMIN_BASE_URL + "/employees/" + employeeId + "/deactivate")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken)).exchange();
            deactivateResponse.expectStatus().isOk();

            var getAfterDeactivate = client.get().uri(ADMIN_BASE_URL + "/employees/" + employeeId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken)).exchange();
            getAfterDeactivate.expectStatus().isOk();
            String deactivatedBody = getAfterDeactivate.returnResult(String.class).getResponseBody();
            assertThat(deactivatedBody).contains("\"active\":false");

            var activateResponse = client.post()
                    .uri(ADMIN_BASE_URL + "/employees/" + employeeId + "/activate")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken)).exchange();
            activateResponse.expectStatus().isOk();

            var getAfterActivate = client.get().uri(ADMIN_BASE_URL + "/employees/" + employeeId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken)).exchange();
            getAfterActivate.expectStatus().isOk();
            String activatedBody = getAfterActivate.returnResult(String.class).getResponseBody();
            assertThat(activatedBody).contains("\"active\":true");
        });
    }

    @Test
    void shouldDeleteEmployee() {
        String employeeSuffix = randomSuffix();

        withAuthenticatedAdmin(accessToken -> {
            registerEmployee(accessToken, employeeSuffix);
            UUID employeeId = employeeIdByEmail(employeeEmailForSuffix(employeeSuffix));

            var deleteResponse = client.delete().uri(ADMIN_BASE_URL + "/employees/" + employeeId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken)).exchange();
            deleteResponse.expectStatus().isOk();

            var getResponse = client.get().uri(ADMIN_BASE_URL + "/employees/" + employeeId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken)).exchange();
            getResponse.expectStatus().isNotFound();
        });
    }

    @Test
    void shouldCreateUpdateAndDeleteCategory() {
        String categorySuffix = randomSuffix();
        String updatedCategorySuffix = randomSuffix();

        withAuthenticatedAdmin(accessToken -> {
            var createResponse = client.post().uri(ADMIN_BASE_URL + "/categories")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .contentType(APPLICATION_JSON).body(createCategoryPayload(categorySuffix))
                    .exchange();
            createResponse.expectStatus().isOk();
            String createBody = createResponse.returnResult(String.class).getResponseBody();
            assertThat(createBody).contains("Category-" + categorySuffix);
            UUID categoryId = categoryIdByName("Category-" + categorySuffix);

            var updateResponse = client.put().uri(ADMIN_BASE_URL + "/categories/" + categoryId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .contentType(APPLICATION_JSON)
                    .body(updateCategoryPayload(updatedCategorySuffix)).exchange();
            updateResponse.expectStatus().isOk();
            String updateBody = updateResponse.returnResult(String.class).getResponseBody();
            assertThat(updateBody).contains("Updated-Category-" + updatedCategorySuffix);

            var deleteResponse = client.delete().uri(ADMIN_BASE_URL + "/categories/" + categoryId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken)).exchange();
            deleteResponse.expectStatus().isOk();
            assertThat(categoryRepository.existsById(categoryId)).isFalse();
        });
    }

    @Test
    void shouldReturnBadRequestWhenCreatingCategoryWithBlankName() {
        withAuthenticatedAdmin(accessToken -> {
            var response = client.post().uri(ADMIN_BASE_URL + "/categories")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .contentType(APPLICATION_JSON).body("{\"name\":\"\"}").exchange();

            response.expectStatus().isBadRequest();
        });
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingUnknownCategory() {
        withAuthenticatedAdmin(accessToken -> {
            var response = client.put().uri(ADMIN_BASE_URL + "/categories/" + randomId())
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .contentType(APPLICATION_JSON).body(updateCategoryPayload(randomSuffix()))
                    .exchange();

            response.expectStatus().isNotFound();
        });
    }

    @Test
    void shouldCreateUpdateActivateDeactivateAndDeleteProduct() {
        String categorySuffix = randomSuffix();
        String productSuffix = randomSuffix();
        String updatedProductSuffix = randomSuffix();

        withAuthenticatedAdmin(accessToken -> {
            UUID categoryId = UUID.fromString(createCategoryAndGetId(accessToken, categorySuffix));

            var createResponse = client.post().uri(ADMIN_BASE_URL + "/products")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .contentType(APPLICATION_JSON)
                    .body(createProductPayload(productSuffix, categoryId)).exchange();
            createResponse.expectStatus().isOk();
            String createBody = createResponse.returnResult(String.class).getResponseBody();
            assertThat(createBody).contains("Product-" + productSuffix);
            UUID productId = productIdBySku("SKU-" + productSuffix);

            var updateResponse = client.put().uri(ADMIN_BASE_URL + "/products/" + productId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .contentType(APPLICATION_JSON)
                    .body(updateProductPayload(updatedProductSuffix, categoryId)).exchange();
            updateResponse.expectStatus().isOk();
            String updateBody = updateResponse.returnResult(String.class).getResponseBody();
            assertThat(updateBody).contains("Updated-Product-" + updatedProductSuffix);

            var deactivateResponse = client.post()
                    .uri(ADMIN_BASE_URL + "/products/" + productId + "/deactivate")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken)).exchange();
            deactivateResponse.expectStatus().isOk();

            var getAfterDeactivate = client.get().uri("/api/v1/products/" + productId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken)).exchange();
            getAfterDeactivate.expectStatus().isOk();
            String deactivatedProductBody = getAfterDeactivate.returnResult(String.class).getResponseBody();
            assertThat(deactivatedProductBody).contains("\"active\":false");

            var activateResponse = client.post()
                    .uri(ADMIN_BASE_URL + "/products/" + productId + "/activate")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken)).exchange();
            activateResponse.expectStatus().isOk();

            var getAfterActivate = client.get().uri("/api/v1/products/" + productId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken)).exchange();
            getAfterActivate.expectStatus().isOk();
            String activatedProductBody = getAfterActivate.returnResult(String.class).getResponseBody();
            assertThat(activatedProductBody).contains("\"active\":true");

            var deleteResponse = client.delete().uri(ADMIN_BASE_URL + "/products/" + productId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken)).exchange();
            deleteResponse.expectStatus().isOk();
            assertThat(productRepository.existsById(productId)).isFalse();
        });
    }

    @Test
    void shouldReturnBadRequestWhenCreatingProductWithInvalidPayload() {
        withAuthenticatedAdmin(accessToken -> {
            String invalidPayload = """
                    {
                        "name": "",
                        "sku": "",
                        "sellingPrice": -1,
                        "costPrice": -1,
                        "quantity": -1,
                        "lowStockThreshold": -1
                    }
                    """;

            var response = client.post().uri(ADMIN_BASE_URL + "/products")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .contentType(APPLICATION_JSON).body(invalidPayload).exchange();

            response.expectStatus().isBadRequest();
        });
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingUnknownProduct() {
        withAuthenticatedAdmin(accessToken -> {
            UUID categoryId = UUID.fromString(createCategoryAndGetId(accessToken, randomSuffix()));

            var response = client.put().uri(ADMIN_BASE_URL + "/products/" + randomId())
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .contentType(APPLICATION_JSON)
                    .body(updateProductPayload(randomSuffix(), categoryId)).exchange();

            response.expectStatus().isNotFound();
        });
    }

    @Test
    void shouldReturnNotFoundWhenAddingImageToUnknownProduct() {
        withAuthenticatedAdmin(accessToken -> {
            UUID missingProductId = randomId();
            String payload = "{\"objectKey\":\"missing-image-key\"}";

            var response = client.post().uri(ADMIN_BASE_URL + "/products/" + missingProductId + "/images")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .contentType(APPLICATION_JSON).body(payload).exchange();

            response.expectStatus().isNotFound();
        });
    }

    @Test
    void shouldReturnAllSalesForAdmin() {
        String categorySuffix = randomSuffix();
        String productSuffix = randomSuffix();

        withAuthenticatedAdmin(accessToken -> {
            UUID categoryId = UUID.fromString(createCategoryAndGetId(accessToken, categorySuffix));

            var createProductResponse = client.post().uri(ADMIN_BASE_URL + "/products")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .contentType(APPLICATION_JSON)
                    .body(createProductPayload(productSuffix, categoryId)).exchange();
            createProductResponse.expectStatus().isOk();
            UUID productId = productIdBySku("SKU-" + productSuffix);

            String salePayload = "{\"productId\":\"" + productId
                    + "\",\"quantity\":3,\"salePrice\":12.5}";
            var createSaleResponse = client.post().uri("/api/v1/sales")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .contentType(APPLICATION_JSON).body(salePayload).exchange();
            createSaleResponse.expectStatus().isOk();

            var response = client.get().uri(ADMIN_BASE_URL + "/sales")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .exchange();

            response.expectStatus().isOk();
            String body = response.returnResult(String.class).getResponseBody();
            assertThat(body).contains(productId.toString());
            assertThat(body).contains("\"quantity\":3");
        });
    }

    @Test
    void shouldReturnAllRestocksForAdmin() {
        String categorySuffix = randomSuffix();
        String productSuffix = randomSuffix();

        withAuthenticatedAdmin(accessToken -> {
            UUID categoryId = UUID.fromString(createCategoryAndGetId(accessToken, categorySuffix));

            var createProductResponse = client.post().uri(ADMIN_BASE_URL + "/products")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .contentType(APPLICATION_JSON)
                    .body(createProductPayload(productSuffix, categoryId)).exchange();
            createProductResponse.expectStatus().isOk();
            UUID productId = productIdBySku("SKU-" + productSuffix);

            String restockPayload = "{\"productId\":\"" + productId + "\",\"quantity\":17}";
            var createRestockResponse = client.post().uri("/api/v1/restocks")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .contentType(APPLICATION_JSON).body(restockPayload).exchange();
            createRestockResponse.expectStatus().isOk();

            var response = client.get().uri(ADMIN_BASE_URL + "/restocks")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .exchange();

            response.expectStatus().isOk();
            String body = response.returnResult(String.class).getResponseBody();
            assertThat(body).contains(productId.toString());
            assertThat(body).contains("\"quantity\":17");
        });
    }

    @Test
    void shouldDeleteSaleByIdForAuthenticatedAdmin() {
        String categorySuffix = randomSuffix();
        String productSuffix = randomSuffix();

        withAuthenticatedAdmin(accessToken -> {
            UUID categoryId = UUID.fromString(createCategoryAndGetId(accessToken, categorySuffix));

            var createProductResponse = client.post().uri(ADMIN_BASE_URL + "/products")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .contentType(APPLICATION_JSON)
                    .body(createProductPayload(productSuffix, categoryId)).exchange();
            createProductResponse.expectStatus().isOk();
            UUID productId = productIdBySku("SKU-" + productSuffix);

            String salePayload = "{\"productId\":\"" + productId
                    + "\",\"quantity\":3,\"salePrice\":22.0}";
            var createSaleResponse = client.post().uri("/api/v1/sales")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .contentType(APPLICATION_JSON).body(salePayload).exchange();
            createSaleResponse.expectStatus().isOk();

            UUID saleId = saleRepository.findAll().stream()
                    .filter(sale -> sale.getProduct() != null
                            && productId.equals(sale.getProduct().getId()))
                    .map(sale -> sale.getId()).reduce((first, second) -> second).orElseThrow();

            var deleteResponse = client.delete().uri(ADMIN_BASE_URL + "/sales/" + saleId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken)).exchange();

            deleteResponse.expectStatus().isOk();
            assertThat(saleRepository.existsById(saleId)).isFalse();
        });
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUnknownSaleIdFromAdminEndpoint() {
        withAuthenticatedAdmin(accessToken -> {
            var response = client.delete().uri(ADMIN_BASE_URL + "/sales/" + UUID.randomUUID())
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .exchange();

            response.expectStatus().isNotFound();
            String body = response.returnResult(String.class).getResponseBody();
            assertThat(body).contains("Sale not found with id");
        });
    }

    @Test
    void shouldDeleteRestockByIdForAuthenticatedAdmin() {
        String categorySuffix = randomSuffix();
        String productSuffix = randomSuffix();

        withAuthenticatedAdmin(accessToken -> {
            UUID categoryId = UUID.fromString(createCategoryAndGetId(accessToken, categorySuffix));

            var createProductResponse = client.post().uri(ADMIN_BASE_URL + "/products")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .contentType(APPLICATION_JSON)
                    .body(createProductPayload(productSuffix, categoryId)).exchange();
            createProductResponse.expectStatus().isOk();
            UUID productId = productIdBySku("SKU-" + productSuffix);

            String restockPayload = "{\"productId\":\"" + productId + "\",\"quantity\":7}";
            var createRestockResponse = client.post().uri("/api/v1/restocks")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .contentType(APPLICATION_JSON).body(restockPayload).exchange();
            createRestockResponse.expectStatus().isOk();

            UUID restockId = restockRepository.findAll().stream()
                    .filter(restock -> restock.getProduct() != null
                            && productId.equals(restock.getProduct().getId()))
                    .map(restock -> restock.getId()).reduce((first, second) -> second).orElseThrow();

            var deleteResponse = client.delete().uri(ADMIN_BASE_URL + "/restocks/" + restockId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken)).exchange();

            deleteResponse.expectStatus().isOk();
            assertThat(restockRepository.existsById(restockId)).isFalse();
        });
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUnknownRestockIdFromAdminEndpoint() {
        withAuthenticatedAdmin(accessToken -> {
            var response = client.delete().uri(ADMIN_BASE_URL + "/restocks/" + UUID.randomUUID())
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .exchange();

            response.expectStatus().isNotFound();
            String body = response.returnResult(String.class).getResponseBody();
            assertThat(body).contains("Restock not found with id");
        });
    }

    @Test
    void shouldGetInventoryValueAsAdmin() {
        withAuthenticatedAdmin(accessToken -> {
            // Create category and product
            UUID categoryId = UUID.fromString(createCategoryAndGetId(accessToken, randomSuffix()));

            var createProductResponse = client.post().uri(ADMIN_BASE_URL + "/products")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .contentType(APPLICATION_JSON)
                    .body(createProductPayload(randomSuffix(), categoryId)).exchange();
            createProductResponse.expectStatus().isOk();

            // Get inventory value
            var response = client.get().uri(ADMIN_BASE_URL + "/inventory/value")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                    .exchange();

            response.expectStatus().isOk();
            String body = response.returnResult(String.class).getResponseBody();
            assertThat(body).contains("totalValue");
            assertThat(body).contains("categories");
        });
    }

    @Test
    void shouldRejectEmployeeAccessToInventoryValue() {
        withAuthenticatedAdmin(adminAccessToken -> {
            String suffix = randomSuffix();
            registerEmployee(adminAccessToken, suffix);
            String employeeEmail = employeeEmailForSuffix(suffix);
            confirmOtpForEmail(employeeEmail);
            String employeeAccessToken = loginByEmail(employeeEmail);

            var response = client.get().uri(ADMIN_BASE_URL + "/inventory/value")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader(employeeAccessToken))
                    .exchange();

            response.expectStatus().isForbidden();
        });
    }
}
