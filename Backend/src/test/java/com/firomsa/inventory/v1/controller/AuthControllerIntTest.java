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
import com.firomsa.inventory.v1.dto.LogoutRequestDTO;
import com.firomsa.inventory.v1.dto.LogoutResponseDTO;
import com.firomsa.inventory.v1.dto.RefreshTokenRequestDTO;
import com.firomsa.inventory.v1.dto.RegisterAdminRequestDTO;
import com.firomsa.inventory.v1.dto.RegisterResponseDTO;
import com.firomsa.inventory.v1.dto.ResendOtpRequestDTO;
import com.firomsa.inventory.v1.dto.ResendOtpResponseDTO;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AuthControllerIntTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ConfirmationOtpRepository confirmationOtpRepository;

    @LocalServerPort
    private Integer port;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1/auth";
    }

    private HttpEntity<?> jsonBody(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private RegisterAdminRequestDTO validRegisterAdminRequest() {
        return new RegisterAdminRequestDTO("John", "Doe", "john_doe", "password123",
                "john.doe@example.com", "+251900000001", "test-bootstrap-token-12345678901234");
    }

    private void registerAdmin() {
        ResponseEntity<RegisterResponseDTO> response = restTemplate.exchange(baseUrl() + "/admins",
                HttpMethod.POST, jsonBody(validRegisterAdminRequest()), RegisterResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    private String latestOtpForEmail(String email) {
        return confirmationOtpRepository.findAll().stream()
                .filter(otp -> otp.getUser() != null && email.equals(otp.getUser().getEmail()))
                .map(otp -> otp.getOtp()).reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException("No OTP found for user " + email));
    }

    private void registerAndConfirmAdmin() {
        registerAdmin();
        String otp = latestOtpForEmail("john.doe@example.com");
        ConfirmOtpRequestDTO confirmRequest = new ConfirmOtpRequestDTO(otp, "john.doe@example.com");

        ResponseEntity<ConfirmOtpResponseDTO> confirmResponse =
                restTemplate.exchange(baseUrl() + "/confirm-otp", HttpMethod.POST,
                        jsonBody(confirmRequest), ConfirmOtpResponseDTO.class);

        assertThat(confirmResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(confirmResponse.getBody()).isNotNull();
        assertThat(confirmResponse.getBody().message()).isEqualTo(
                "Successfully confirmed OTP, please login using your email and password");
    }

    @Test
    void shouldRegisterAdminWhenRequestIsValid() {
        // Arrange
        RegisterAdminRequestDTO request = validRegisterAdminRequest();

        // Act
        ResponseEntity<RegisterResponseDTO> response = restTemplate.exchange(baseUrl() + "/admins",

                HttpMethod.POST, new HttpEntity<>(request), RegisterResponseDTO.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .isEqualTo("You have successfully registered, confirm the OTP sent to your email");
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().getFirstName()).isEqualTo("John");
        assertThat(response.getBody().data().getLastName()).isEqualTo("Doe");
        assertThat(response.getBody().data().getUsername()).isEqualTo("john_doe");
        assertThat(response.getBody().data().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(response.getBody().data().getRole()).isEqualTo("ADMIN");
    }

    @Test
    void shouldConfirmOtpWhenOtpIsValid() {
        registerAdmin();
        String otp = latestOtpForEmail("john.doe@example.com");
        ConfirmOtpRequestDTO request = new ConfirmOtpRequestDTO(otp, "john.doe@example.com");

        ResponseEntity<ConfirmOtpResponseDTO> response =
                restTemplate.exchange(baseUrl() + "/confirm-otp", HttpMethod.POST,
                        jsonBody(request), ConfirmOtpResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo(
                "Successfully confirmed OTP, please login using your email and password");
    }

    @Test
    void shouldLoginRefreshAndLogoutWhenFlowIsValid() {
        registerAndConfirmAdmin();

        LoginRequestDTO loginRequest = new LoginRequestDTO("password123", "john.doe@example.com");
        ResponseEntity<LoginResponseDTO> loginResponse = restTemplate.exchange(baseUrl() + "/login",
                HttpMethod.POST, jsonBody(loginRequest), LoginResponseDTO.class);

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).isNotNull();
        assertThat(loginResponse.getBody().accessToken()).isNotBlank();
        assertThat(loginResponse.getBody().refreshToken()).isNotBlank();
        assertThat(loginResponse.getBody().email()).isEqualTo("john.doe@example.com");

        String refreshToken = loginResponse.getBody().refreshToken();

        RefreshTokenRequestDTO refreshRequest =
                new RefreshTokenRequestDTO(refreshToken, "john.doe@example.com");
        ResponseEntity<LoginResponseDTO> refreshResponse =
                restTemplate.exchange(baseUrl() + "/refresh", HttpMethod.POST,
                        jsonBody(refreshRequest), LoginResponseDTO.class);

        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refreshResponse.getBody()).isNotNull();
        assertThat(refreshResponse.getBody().accessToken()).isNotBlank();
        assertThat(refreshResponse.getBody().refreshToken()).isEqualTo(refreshToken);

        LogoutRequestDTO logoutRequest = new LogoutRequestDTO(refreshToken, "john.doe@example.com");
        ResponseEntity<LogoutResponseDTO> logoutResponse =
                restTemplate.exchange(baseUrl() + "/logout", HttpMethod.POST,
                        jsonBody(logoutRequest), LogoutResponseDTO.class);

        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(logoutResponse.getBody()).isNotNull();
        assertThat(logoutResponse.getBody().message()).isEqualTo("Successfully logged out");
    }

    @Test
    void shouldReturnBadRequestWhenBootstrapTokenIsInvalid() {
        RegisterAdminRequestDTO request = new RegisterAdminRequestDTO("John", "Doe", "john_doe",
                "password123", "john.doe@example.com", "+251900000001",
                "wrong-bootstrap-token-123456789012");

        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/admins",
                HttpMethod.POST, jsonBody(request), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnBadRequestWhenConfirmOtpPayloadIsInvalid() {
        ConfirmOtpRequestDTO request = new ConfirmOtpRequestDTO("", "");

        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/confirm-otp",
                HttpMethod.POST, jsonBody(request), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test

    void shouldReturnBadRequestWhenConfirmOtpCodeIsInvalid() {
        registerAdmin();
        ConfirmOtpRequestDTO request = new ConfirmOtpRequestDTO("00000", "john.doe@example.com");

        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/confirm-otp",
                HttpMethod.POST, jsonBody(request), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnBadRequestWhenResendOtpPayloadIsInvalid() {
        ResendOtpRequestDTO request = new ResendOtpRequestDTO("invalid-email");

        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/resend-otp",
                HttpMethod.POST, jsonBody(request), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test

    void shouldResendOtpWhenEmailExists() {
        registerAdmin();
        ResendOtpRequestDTO request = new ResendOtpRequestDTO("john.doe@example.com");

        ResponseEntity<ResendOtpResponseDTO> response =
                restTemplate.exchange(baseUrl() + "/resend-otp", HttpMethod.POST, jsonBody(request),
                        ResendOtpResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .isEqualTo("Successfully resent OTP, check your inbox");
    }

    @Test

    void shouldReturnNotFoundWhenResendOtpEmailDoesNotExist() {
        ResendOtpRequestDTO request = new ResendOtpRequestDTO("unknown.user@example.com");

        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/resend-otp",
                HttpMethod.POST, jsonBody(request), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test

    void shouldReturnBadRequestWhenSecondAdminRegistrationAttempted() {
        registerAdmin();

        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/admins",
                HttpMethod.POST, jsonBody(validRegisterAdminRequest()), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnBadRequestWhenLoginPayloadIsInvalid() {
        LoginRequestDTO request = new LoginRequestDTO("short", "invalid-email");

        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/login",
                HttpMethod.POST, jsonBody(request), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnBadRequestWhenRefreshPayloadIsInvalid() {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO(null, "invalid-email");

        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/refresh",
                HttpMethod.POST, jsonBody(request), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnBadRequestWhenLogoutPayloadIsInvalid() {
        LogoutRequestDTO request = new LogoutRequestDTO(null, "invalid-email");

        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/logout",
                HttpMethod.POST, jsonBody(request), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

}
