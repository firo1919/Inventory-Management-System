package com.firomsa.inventory.v1.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import com.firomsa.inventory.repository.ConfirmationOtpRepository;
import com.firomsa.inventory.v1.dto.ConfirmOtpRequestDTO;
import com.firomsa.inventory.v1.dto.ConfirmOtpResponseDTO;
import com.firomsa.inventory.v1.dto.LoginRequestDTO;
import com.firomsa.inventory.v1.dto.LoginResponseDTO;
import com.firomsa.inventory.v1.dto.RegisterAdminRequestDTO;
import com.firomsa.inventory.v1.dto.RegisterResponseDTO;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AdminControllerIntTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ConfirmationOtpRepository confirmationOtpRepository;

    @LocalServerPort
    private Integer port;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1/admin";
    }

    private HttpEntity<String> emptyJsonBody() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>("{}", headers);
    }

    private HttpEntity<?> jsonBody(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<?> authorizedJsonBody(Object body, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Void> authorizedRequest(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return new HttpEntity<>(headers);
    }

    private RegisterAdminRequestDTO validRegisterAdminRequest() {
        return new RegisterAdminRequestDTO("John", "Doe", "john_doe", "password123",
                "john.doe@example.com", "+251900000001", "test-bootstrap-token-12345678901234");
    }

    private String latestOtpForEmail(String email) {
        return confirmationOtpRepository.findAll().stream()
                .filter(otp -> otp.getUser() != null && email.equals(otp.getUser().getEmail()))
                .map(otp -> otp.getOtp()).reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException("No OTP found for user " + email));
    }

    private String loginAsAdminAccessToken() {
        ResponseEntity<RegisterResponseDTO> registerResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/auth/admins", HttpMethod.POST,
                jsonBody(validRegisterAdminRequest()), RegisterResponseDTO.class);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        String otp = latestOtpForEmail("john.doe@example.com");
        ConfirmOtpRequestDTO confirmRequest = new ConfirmOtpRequestDTO(otp, "john.doe@example.com");
        ResponseEntity<ConfirmOtpResponseDTO> confirmResponse =
                restTemplate.exchange("http://localhost:" + port + "/api/v1/auth/confirm-otp",
                        HttpMethod.POST, jsonBody(confirmRequest), ConfirmOtpResponseDTO.class);
        assertThat(confirmResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        LoginRequestDTO loginRequest = new LoginRequestDTO("password123", "john.doe@example.com");
        ResponseEntity<LoginResponseDTO> loginResponse =
                restTemplate.exchange("http://localhost:" + port + "/api/v1/auth/login",
                        HttpMethod.POST, jsonBody(loginRequest), LoginResponseDTO.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).isNotNull();
        return loginResponse.getBody().accessToken();
    }

    @Test
    void shouldRejectUnauthorizedRegisterEmployee() {
        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/employees",
                HttpMethod.POST, emptyJsonBody(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUnauthorizedGetAllEmployees() {
        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/employees",
                HttpMethod.GET, HttpEntity.EMPTY, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUnauthorizedGetEmployeeById() {
        ResponseEntity<String> response =
                restTemplate.exchange(baseUrl() + "/employees/" + UUID.randomUUID(), HttpMethod.GET,
                        HttpEntity.EMPTY, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUnauthorizedUpdateEmployee() {
        ResponseEntity<String> response =
                restTemplate.exchange(baseUrl() + "/employees/" + UUID.randomUUID(), HttpMethod.PUT,
                        emptyJsonBody(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUnauthorizedDeactivateEmployee() {
        ResponseEntity<String> response =
                restTemplate.exchange(baseUrl() + "/employees/" + UUID.randomUUID() + "/deactivate",
                        HttpMethod.POST, HttpEntity.EMPTY, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUnauthorizedActivateEmployee() {
        ResponseEntity<String> response =
                restTemplate.exchange(baseUrl() + "/employees/" + UUID.randomUUID() + "/activate",
                        HttpMethod.POST, HttpEntity.EMPTY, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUnauthorizedDeleteEmployee() {
        ResponseEntity<String> response =
                restTemplate.exchange(baseUrl() + "/employees/" + UUID.randomUUID(),
                        HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUnauthorizedCreateProduct() {
        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/products",
                HttpMethod.POST, emptyJsonBody(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUnauthorizedUpdateProduct() {
        ResponseEntity<String> response =
                restTemplate.exchange(baseUrl() + "/products/" + UUID.randomUUID(), HttpMethod.PUT,
                        emptyJsonBody(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUnauthorizedDeleteProduct() {
        ResponseEntity<String> response =
                restTemplate.exchange(baseUrl() + "/products/" + UUID.randomUUID(),
                        HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUnauthorizedActivateProduct() {
        ResponseEntity<String> response =
                restTemplate.exchange(baseUrl() + "/products/" + UUID.randomUUID() + "/activate",
                        HttpMethod.POST, HttpEntity.EMPTY, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUnauthorizedDeactivateProduct() {
        ResponseEntity<String> response =
                restTemplate.exchange(baseUrl() + "/products/" + UUID.randomUUID() + "/deactivate",
                        HttpMethod.POST, HttpEntity.EMPTY, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUnauthorizedAddProductImage() {
        ResponseEntity<String> response =
                restTemplate.exchange(baseUrl() + "/products/" + UUID.randomUUID() + "/images",
                        HttpMethod.POST, emptyJsonBody(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUnauthorizedCreateCategory() {
        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/categories",
                HttpMethod.POST, emptyJsonBody(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUnauthorizedUpdateCategory() {
        ResponseEntity<String> response =
                restTemplate.exchange(baseUrl() + "/categories/" + UUID.randomUUID(),
                        HttpMethod.PUT, emptyJsonBody(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUnauthorizedDeleteCategory() {
        ResponseEntity<String> response =
                restTemplate.exchange(baseUrl() + "/categories/" + UUID.randomUUID(),
                        HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnBadRequestWhenCreateEmployeePayloadIsInvalid() {
        String accessToken = loginAsAdminAccessToken();

        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/employees",
                HttpMethod.POST, authorizedJsonBody(new Object(), accessToken), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnBadRequestWhenCreateProductPayloadIsInvalid() {
        String accessToken = loginAsAdminAccessToken();

        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/products",
                HttpMethod.POST, authorizedJsonBody(new Object(), accessToken), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnBadRequestWhenCreateCategoryPayloadIsInvalid() {
        String accessToken = loginAsAdminAccessToken();

        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/categories",
                HttpMethod.POST, authorizedJsonBody(new Object(), accessToken), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnBadRequestWhenEmployeeIdIsMalformed() {
        String accessToken = loginAsAdminAccessToken();

        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/employees/not-a-uuid",
                HttpMethod.GET, authorizedRequest(accessToken), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
