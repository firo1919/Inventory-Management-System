package com.firomsa.inventory.v1.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

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
import org.springframework.web.util.UriComponentsBuilder;

import com.firomsa.inventory.repository.ConfirmationOtpRepository;
import com.firomsa.inventory.v1.dto.ConfirmOtpRequestDTO;
import com.firomsa.inventory.v1.dto.ConfirmOtpResponseDTO;
import com.firomsa.inventory.v1.dto.FileDTO;
import com.firomsa.inventory.v1.dto.LoginRequestDTO;
import com.firomsa.inventory.v1.dto.LoginResponseDTO;
import com.firomsa.inventory.v1.dto.ProfileUpdateDTO;
import com.firomsa.inventory.v1.dto.RegisterAdminRequestDTO;
import com.firomsa.inventory.v1.dto.RegisterResponseDTO;
import com.firomsa.inventory.v1.dto.UploadRequestDTO;
import com.firomsa.inventory.v1.dto.UploadResponseDTO;
import com.firomsa.inventory.v1.dto.UserResponseDTO;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ProfileControllerIntTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ConfirmationOtpRepository confirmationOtpRepository;

    @LocalServerPort
    private Integer port;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1/profile";
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

    private String uploadImageAsAdmin(String accessToken) {
        UploadRequestDTO uploadRequest = new UploadRequestDTO("profile.png", "image/png");
        ResponseEntity<UploadResponseDTO> presignResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/uploads/presign", HttpMethod.POST,
                authorizedJsonBody(uploadRequest, accessToken), UploadResponseDTO.class);

        assertThat(presignResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(presignResponse.getBody()).isNotNull();

        byte[] content = "dummy-image-content".getBytes();
        HttpHeaders putHeaders = new HttpHeaders();
        putHeaders.setContentType(MediaType.IMAGE_PNG);
        HttpEntity<byte[]> putBody = new HttpEntity<>(content, putHeaders);

        URI presignedUri = URI.create(presignResponse.getBody().uploadUrl());
        ResponseEntity<String> uploadResponse =
                restTemplate.exchange(presignedUri, HttpMethod.PUT, putBody, String.class);

        if (uploadResponse.getStatusCode().is3xxRedirection()
                && uploadResponse.getHeaders().getLocation() != null) {
            URI redirectedLocation = uploadResponse.getHeaders().getLocation();
            if (redirectedLocation.getQuery() == null && presignedUri.getQuery() != null) {
                redirectedLocation = UriComponentsBuilder.fromUri(redirectedLocation)
                        .replaceQuery(presignedUri.getQuery()).build(true).toUri();
            }
            uploadResponse = restTemplate.exchange(redirectedLocation, HttpMethod.PUT, putBody,
                    String.class);
        }

        assertThat(uploadResponse.getStatusCode().value())
                .as("Expected presigned upload to succeed, but got status %s with body %s",
                        uploadResponse.getStatusCode(), uploadResponse.getBody())
                .isIn(200, 201, 204);

        return presignResponse.getBody().objectKey();
    }

    @Test
    void shouldRejectUnauthorizedGetProfile() {
        ResponseEntity<String> response =
                restTemplate.exchange(baseUrl(), HttpMethod.GET, HttpEntity.EMPTY, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUnauthorizedUpdateProfile() {
        ResponseEntity<String> response =
                restTemplate.exchange(baseUrl(), HttpMethod.PUT, emptyJsonBody(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUnauthorizedAddProfilePicture() {
        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/profile-picture",
                HttpMethod.POST, emptyJsonBody(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldGetProfileWhenAuthenticated() {
        String accessToken = loginAsAdminAccessToken();

        ResponseEntity<UserResponseDTO> response = restTemplate.exchange(baseUrl(), HttpMethod.GET,
                authorizedRequest(accessToken), UserResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(response.getBody().getUsername()).isEqualTo("john_doe");
    }

    @Test
    void shouldUpdateProfileWhenAuthenticated() {
        String accessToken = loginAsAdminAccessToken();
        ProfileUpdateDTO request = new ProfileUpdateDTO("Johnny", "Doer", "johnny_doe",
                "newpassword123", "johnny.doe@example.com", "+251900000002");

        ResponseEntity<UserResponseDTO> response = restTemplate.exchange(baseUrl(), HttpMethod.PUT,
                authorizedJsonBody(request, accessToken), UserResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFirstName()).isEqualTo("Johnny");
        assertThat(response.getBody().getUsername()).isEqualTo("johnny_doe");
        assertThat(response.getBody().getEmail()).isEqualTo("johnny.doe@example.com");
    }

    @Test
    void shouldAddProfilePictureWhenAuthenticated() {
        String accessToken = loginAsAdminAccessToken();
        String objectKey = uploadImageAsAdmin(accessToken);
        FileDTO request = new FileDTO(objectKey);

        ResponseEntity<UserResponseDTO> response =
                restTemplate.exchange(baseUrl() + "/profile-picture", HttpMethod.POST,
                        authorizedJsonBody(request, accessToken), UserResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProfilePictureUrl()).isNotBlank();
    }

    @Test
    void shouldReturnBadRequestWhenProfileUpdatePayloadIsInvalid() {
        String accessToken = loginAsAdminAccessToken();
        ProfileUpdateDTO invalidRequest =
                new ProfileUpdateDTO("", "", "", "short", "invalid-email", "");

        ResponseEntity<String> response = restTemplate.exchange(baseUrl(), HttpMethod.PUT,
                authorizedJsonBody(invalidRequest, accessToken), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnBadRequestWhenProfilePicturePayloadIsInvalid() {
        String accessToken = loginAsAdminAccessToken();
        FileDTO invalidRequest = new FileDTO("");

        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/profile-picture",
                HttpMethod.POST, authorizedJsonBody(invalidRequest, accessToken), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnNotFoundWhenProfilePictureObjectDoesNotExist() {
        String accessToken = loginAsAdminAccessToken();
        FileDTO request = new FileDTO("missing/object-key.png");

        ResponseEntity<String> response = restTemplate.exchange(baseUrl() + "/profile-picture",
                HttpMethod.POST, authorizedJsonBody(request, accessToken), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
