package com.firomsa.inventory.v1.controller.e2eTest;

import static org.assertj.core.api.Assertions.assertThat;
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

public class AdminControllerE2ETest extends AbstractE2ETest {

    private static final String AUTH_BASE_URL = "/api/v1/auth";
    private static final String ADMIN_BASE_URL = "/api/v1/admin";
    private static final String BOOTSTRAP_TOKEN = "test-bootstrap-token-12345678901234";
    private static final String DEFAULT_PASSWORD = "password123";

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

    private String adminEmailForSuffix(String suffix) {
        return "admin." + suffix + "@example.com";
    }

    private String employeeEmailForSuffix(String suffix) {
        return "employee_" + suffix + "@example.com";
    }

    private String latestOtpForEmail(String email) {
        UUID userId = userRepository.findByEmail(email).orElseThrow().getId();
        return confirmationOtpRepository.findAll().stream()
                .filter(otp -> otp.getUser() != null && userId.equals(otp.getUser().getId()))
                .map(otp -> otp.getOtp()).reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException("No OTP found for user " + email));
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
    }

    private String loginAsAdmin(String suffix) {
        String email = adminEmailForSuffix(suffix);
        String loginPayload =
                "{\"password\":\"" + DEFAULT_PASSWORD + "\",\"email\":\"" + email + "\"}";

        var loginResponse = client.post().uri(AUTH_BASE_URL + "/login")
                .contentType(APPLICATION_JSON).body(loginPayload).exchange();

        loginResponse.expectStatus().isOk();
        String body = loginResponse.returnResult(String.class).getResponseBody();
        assertThat(body).contains("\"accessToken\":");
        String tokenMarker = "\"accessToken\":\"";
        int start = body.indexOf(tokenMarker);
        assertThat(start).isGreaterThanOrEqualTo(0);
        int from = start + tokenMarker.length();
        int end = body.indexOf('"', from);
        assertThat(end).isGreaterThan(from);
        return body.substring(from, end);
    }

    private void registerEmployee(String accessToken, String suffix) {
        var employeeResponse =
                client.post().uri(ADMIN_BASE_URL + "/employees").contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .body(registerEmployeePayload(suffix)).exchange();

        employeeResponse.expectStatus().isOk();
        String employeeBody = employeeResponse.returnResult(String.class).getResponseBody();
        assertThat(employeeBody).contains(employeeEmailForSuffix(suffix));
    }

    @Test
    void shouldReturnAllEmployees() {
        String adminSuffix = randomSuffix();
        String employeeSuffixOne = randomSuffix();
        String employeeSuffixTwo = randomSuffix();

        registerAndConfirmAdmin(adminSuffix);
        String accessToken = loginAsAdmin(adminSuffix);

        registerEmployee(accessToken, employeeSuffixOne);
        registerEmployee(accessToken, employeeSuffixTwo);

        var response = client.get().uri(ADMIN_BASE_URL + "/employees")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).exchange();

        response.expectStatus().isOk();
        String responseBody = response.returnResult(String.class).getResponseBody();
        assertThat(responseBody).contains(employeeEmailForSuffix(employeeSuffixOne));
        assertThat(responseBody).contains(employeeEmailForSuffix(employeeSuffixTwo));
    }

}
