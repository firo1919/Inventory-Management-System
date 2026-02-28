package com.firomsa.inventory.v1.controller;

import static org.assertj.core.api.Assertions.assertThat;

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
import com.firomsa.inventory.v1.dto.UploadRequestDTO;
import com.firomsa.inventory.v1.dto.UploadResponseDTO;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UploadControllerIntTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ConfirmationOtpRepository confirmationOtpRepository;

    @LocalServerPort
    private Integer port;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1/uploads";
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

    private HttpEntity<?> invalidBearerJsonBody(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("invalid.token.value");
        return new HttpEntity<>(body, headers);
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
    void shouldRejectUnauthorizedCreateUploadPresignTicket() {
        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/presign",
                HttpMethod.POST, emptyJsonBody(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldCreateUploadPresignTicketWhenAuthenticated() {
        String accessToken = loginAsAdminAccessToken();
        UploadRequestDTO request = new UploadRequestDTO("avatar.png", "image/png");

        ResponseEntity<UploadResponseDTO> response = restTemplate.exchange(baseUrl() + "/presign",
                HttpMethod.POST, authorizedJsonBody(request, accessToken), UploadResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().objectKey()).isNotBlank();
        assertThat(response.getBody().uploadUrl()).isNotBlank();
        assertThat(response.getBody().expiresIn()).isNotBlank();
    }

    @Test
    void shouldReturnBadRequestWhenUploadPayloadIsInvalid() {
        String accessToken = loginAsAdminAccessToken();
        UploadRequestDTO invalidRequest = new UploadRequestDTO("", "");

        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/presign",
                HttpMethod.POST, authorizedJsonBody(invalidRequest, accessToken), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnUnauthorizedWhenBearerTokenIsInvalid() {
        UploadRequestDTO request = new UploadRequestDTO("avatar.png", "image/png");

        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/presign",
                HttpMethod.POST, invalidBearerJsonBody(request), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldGenerateDifferentObjectKeysForSameFilename() {
        String accessToken = loginAsAdminAccessToken();
        UploadRequestDTO request = new UploadRequestDTO("avatar.png", "image/png");

        ResponseEntity<UploadResponseDTO> firstResponse =
                restTemplate.exchange(baseUrl() + "/presign", HttpMethod.POST,
                        authorizedJsonBody(request, accessToken), UploadResponseDTO.class);
        ResponseEntity<UploadResponseDTO> secondResponse =
                restTemplate.exchange(baseUrl() + "/presign", HttpMethod.POST,
                        authorizedJsonBody(request, accessToken), UploadResponseDTO.class);

        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(firstResponse.getBody()).isNotNull();
        assertThat(secondResponse.getBody()).isNotNull();
        assertThat(firstResponse.getBody().objectKey())
                .isNotEqualTo(secondResponse.getBody().objectKey());
    }
}
