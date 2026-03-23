package com.firomsa.inventory.v1.controller.e2eTest;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

public class EmployeeControllerE2ETest extends AbstractE2ETest {

    private static final String EMPLOYEE_BASE_URL = "/api/v1/employee";

    private static String adminAccessToken;

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
        var response = client.get().uri(EMPLOYEE_BASE_URL).exchange();
        response.expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnForbiddenForEmployeeBaseWhenUsingAdminToken() {
        registerAndConfirmAdminIfNeeded();

        var response = client.get().uri(EMPLOYEE_BASE_URL)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(adminAccessToken))
                .exchange();

        response.expectStatus().isForbidden();
    }

    @Test
    void shouldReachControllerLayerForEmployeeToken() {
        registerAndConfirmAdminIfNeeded();
        String employeeToken = registerAndLoginEmployee(adminAccessToken, randomSuffix());

        var response = client.get().uri(EMPLOYEE_BASE_URL)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader(employeeToken)).exchange();

        response.expectStatus().is5xxServerError();
    }
}
