package com.firomsa.inventory.v1.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;
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
import com.firomsa.inventory.v1.dto.CategoryRequestDTO;
import com.firomsa.inventory.v1.dto.CategoryResponseDTO;
import com.firomsa.inventory.v1.dto.ConfirmOtpRequestDTO;
import com.firomsa.inventory.v1.dto.ConfirmOtpResponseDTO;
import com.firomsa.inventory.v1.dto.LoginRequestDTO;
import com.firomsa.inventory.v1.dto.LoginResponseDTO;
import com.firomsa.inventory.v1.dto.ProductRequestDTO;
import com.firomsa.inventory.v1.dto.ProductResponseDTO;
import com.firomsa.inventory.v1.dto.RegisterAdminRequestDTO;
import com.firomsa.inventory.v1.dto.RegisterResponseDTO;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ProductControllerIntTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ConfirmationOtpRepository confirmationOtpRepository;

    @LocalServerPort
    private Integer port;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1/products";
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

    private UUID createCategoryAsAdmin(String accessToken, String name) {
        CategoryRequestDTO request = new CategoryRequestDTO(name);
        ResponseEntity<CategoryResponseDTO> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/admin/categories", HttpMethod.POST,
                authorizedJsonBody(request, accessToken), CategoryResponseDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody().getId();
    }

    private ProductResponseDTO createProductAsAdmin(String accessToken) {
        UUID categoryId = createCategoryAsAdmin(accessToken, "Electronics");
        ProductRequestDTO request = ProductRequestDTO.builder().name("Wireless Mouse")
                .sku("SKU-WM-001").description("Ergonomic mouse")
                .sellingPrice(new BigDecimal("49.99")).costPrice(new BigDecimal("25.00"))
                .quantity(15).lowStockThreshold(3).categoryIds(Set.of(categoryId)).build();

        ResponseEntity<ProductResponseDTO> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/admin/products", HttpMethod.POST,
                authorizedJsonBody(request, accessToken), ProductResponseDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    @Test
    void shouldRejectUnauthorizedGetAllProducts() {
        ResponseEntity<String> response =
                restTemplate.exchange(baseUrl(), HttpMethod.GET, HttpEntity.EMPTY, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUnauthorizedGetProductById() {
        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/" + UUID.randomUUID(),
                HttpMethod.GET, HttpEntity.EMPTY, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldGetAllProductsWhenAuthenticated() {
        String accessToken = loginAsAdminAccessToken();
        createProductAsAdmin(accessToken);

        ResponseEntity<ProductResponseDTO[]> response = restTemplate.exchange(baseUrl(),
                HttpMethod.GET, authorizedRequest(accessToken), ProductResponseDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody()).extracting(ProductResponseDTO::getName)
                .contains("Wireless Mouse");
    }

    @Test
    void shouldReturnEmptyProductListWhenAuthenticatedAndNoProductExists() {
        String accessToken = loginAsAdminAccessToken();

        ResponseEntity<ProductResponseDTO[]> response = restTemplate.exchange(baseUrl(),
                HttpMethod.GET, authorizedRequest(accessToken), ProductResponseDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void shouldGetProductByIdWhenAuthenticated() {
        String accessToken = loginAsAdminAccessToken();
        ProductResponseDTO created = createProductAsAdmin(accessToken);

        ResponseEntity<ProductResponseDTO> response =
                restTemplate.exchange(baseUrl() + "/" + created.getId(), HttpMethod.GET,
                        authorizedRequest(accessToken), ProductResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(created.getId());
        assertThat(response.getBody().getSku()).isEqualTo("SKU-WM-001");
    }

    @Test
    void shouldReturnBadRequestWhenProductIdIsMalformed() {
        String accessToken = loginAsAdminAccessToken();

        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/not-a-uuid",
                HttpMethod.GET, authorizedRequest(accessToken), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() {
        String accessToken = loginAsAdminAccessToken();

        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/" + UUID.randomUUID(),
                HttpMethod.GET, authorizedRequest(accessToken), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
