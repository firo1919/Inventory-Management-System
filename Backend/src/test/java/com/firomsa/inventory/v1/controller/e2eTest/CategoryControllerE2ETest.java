package com.firomsa.inventory.v1.controller.e2eTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

public class CategoryControllerE2ETest extends AbstractE2ETest {

    private static final String CATEGORY_BASE_URL = "/api/v1/categories";

    private String adminAccessToken;

    private String createCategoryPayload(String suffix) {
        return "{\"name\":\"Category-" + suffix + "\"}";
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

    private String createCategoryAndGetId(String accessToken, String suffix) {
        var createResponse = client.post().uri(ADMIN_BASE_URL + "/categories")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(accessToken))
                .contentType(APPLICATION_JSON).body(createCategoryPayload(suffix)).exchange();
        createResponse.expectStatus().isOk();

        String categoryName = "Category-" + suffix;
        UUID categoryId = categoryRepository.findAll().stream()
                .filter(category -> categoryName.equals(category.getName()))
                .map(category -> category.getId()).findFirst().orElseThrow();
        return categoryId.toString();
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
        var response = client.get().uri(CATEGORY_BASE_URL).exchange();
        response.expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnAllCategoriesForAdmin() {
        registerAndConfirmAdminIfNeeded();
        String suffix = randomSuffix();
        createCategoryAndGetId(adminAccessToken, suffix);

        var response = client.get().uri(CATEGORY_BASE_URL)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(adminAccessToken))
                .exchange();

        response.expectStatus().isOk();
        String body = response.returnResult(String.class).getResponseBody();
        assertThat(body).contains("Category-" + suffix);
    }

    @Test
    void shouldReturnCategoryByIdForAuthenticatedUser() {
        registerAndConfirmAdminIfNeeded();
        String suffix = randomSuffix();
        String categoryId = createCategoryAndGetId(adminAccessToken, suffix);

        var response = client.get().uri(CATEGORY_BASE_URL + "/" + categoryId)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(adminAccessToken))
                .exchange();

        response.expectStatus().isOk();
        String body = response.returnResult(String.class).getResponseBody();
        assertThat(body).contains("Category-" + suffix);
    }

    @Test
    void shouldReturnNotFoundForUnknownCategoryId() {
        registerAndConfirmAdminIfNeeded();

        var response = client.get().uri(CATEGORY_BASE_URL + "/" + UUID.randomUUID())
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(adminAccessToken))
                .exchange();

        response.expectStatus().isNotFound();
    }

    @Test
    void shouldAllowEmployeeToReadCategories() {
        registerAndConfirmAdminIfNeeded();
        String suffix = randomSuffix();
        createCategoryAndGetId(adminAccessToken, suffix);
        String employeeToken = registerAndLoginEmployee(adminAccessToken, randomSuffix());

        var response = client.get().uri(CATEGORY_BASE_URL)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(employeeToken)).exchange();

        response.expectStatus().isOk();
    }
}
