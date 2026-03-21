package com.firomsa.inventory.v1.controller.e2eTest;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.client.RestTestClient;

import com.firomsa.inventory.repository.CategoryRepository;
import com.firomsa.inventory.repository.ConfirmationOtpRepository;
import com.firomsa.inventory.repository.ProductRepository;
import com.firomsa.inventory.repository.RefreshTokenRepository;
import com.firomsa.inventory.repository.UserRepository;

public class EmployeeControllerE2ETest extends AbstractE2ETest {

    private static final String AUTH_BASE_URL = "/api/v1/auth";
    private static final String ADMIN_BASE_URL = "/api/v1/admin";
    private static final String EMPLOYEE_BASE_URL = "/api/v1/employee";
    private static final String BOOTSTRAP_TOKEN = "test-bootstrap-token-12345678901234";
    private static final String DEFAULT_PASSWORD = "password123";

    private static String adminAccessToken;

    private RestTestClient client;

    @Autowired
    private ConfirmationOtpRepository confirmationOtpRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void setUpClient() {
        this.client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAllInBatch();
        confirmationOtpRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    private String randomSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private String authorizationHeader(String accessToken) {
        return "Bearer " + accessToken;
    }

    private String adminEmailForSuffix(String suffix) {
        return "admin." + suffix + "@example.com";
    }

    private String employeeEmailForSuffix(String suffix) {
        return "employee_" + suffix + "@example.com";
    }

    private String registerAdminPayload(String suffix) {
        return """
                {
                    "firstName": "Admin",
                    "lastName": "User",
                    "username": "admin_%s",
                    "password": "%s",
                    "email": "admin.%s@example.com",
                    "phone": "+251900%s",
                    "bootstrapToken": "%s"
                }
                """.formatted(suffix, DEFAULT_PASSWORD, suffix, suffix.substring(0, 6),
                BOOTSTRAP_TOKEN);
    }

    private String registerEmployeePayload(String suffix) {
        return """
                {
                    "firstName": "EmpFirst%s",
                    "lastName": "EmpLast%s",
                    "username": "employee_%s",
                    "password": "%s",
                    "email": "employee_%s@example.com",
                    "role": "EMPLOYEE",
                    "phone": "+251911%s"
                }
                """.formatted(suffix, suffix, suffix, DEFAULT_PASSWORD, suffix,
                suffix.substring(0, 6));
    }

    private String latestOtpForEmail(String email) {
        UUID userId = userRepository.findByEmail(email).orElseThrow().getId();
        return confirmationOtpRepository.findAll().stream()
                .filter(otp -> otp.getUser() != null && userId.equals(otp.getUser().getId()))
                .map(otp -> otp.getOtp()).reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException("No OTP found for user " + email));
    }

    private String extractAccessToken(String body) {
        String tokenMarker = "\"accessToken\":\"";
        int start = body.indexOf(tokenMarker);
        int from = start + tokenMarker.length();
        int end = body.indexOf('"', from);
        return body.substring(from, end);
    }

    private String loginByEmail(String email) {
        String payload = "{\"password\":\"" + DEFAULT_PASSWORD + "\",\"email\":\"" + email + "\"}";
        var response = client.post().uri(AUTH_BASE_URL + "/login").contentType(APPLICATION_JSON)
                .body(payload).exchange();
        response.expectStatus().isOk();
        return extractAccessToken(response.returnResult(String.class).getResponseBody());
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
