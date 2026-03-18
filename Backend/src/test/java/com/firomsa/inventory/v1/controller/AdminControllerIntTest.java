package com.firomsa.inventory.v1.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import com.firomsa.inventory.repository.ConfirmationOtpRepository;
import com.firomsa.inventory.repository.CategoryRepository;
import com.firomsa.inventory.repository.ProductRepository;
import com.firomsa.inventory.repository.UserRepository;
import com.firomsa.inventory.model.Roles;
import com.firomsa.inventory.v1.dto.CategoryRequestDTO;
import com.firomsa.inventory.v1.dto.CategoryResponseDTO;
import com.firomsa.inventory.v1.dto.CategoryUpdateRequestDTO;
import com.firomsa.inventory.v1.dto.ConfirmOtpRequestDTO;
import com.firomsa.inventory.v1.dto.ConfirmOtpResponseDTO;
import com.firomsa.inventory.v1.dto.FileDTO;
import com.firomsa.inventory.v1.dto.LoginRequestDTO;
import com.firomsa.inventory.v1.dto.LoginResponseDTO;
import com.firomsa.inventory.v1.dto.ProductRequestDTO;
import com.firomsa.inventory.v1.dto.ProductResponseDTO;
import com.firomsa.inventory.v1.dto.ProductUpdateRequestDTO;
import com.firomsa.inventory.v1.dto.RegisterAdminRequestDTO;
import com.firomsa.inventory.v1.dto.RegisterRequestDTO;
import com.firomsa.inventory.v1.dto.RegisterResponseDTO;
import com.firomsa.inventory.v1.dto.UserResponseDTO;
import com.firomsa.inventory.v1.dto.UserUpdateRequestDTO;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AdminControllerIntTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ConfirmationOtpRepository confirmationOtpRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Value("${spring.cloud.aws.s3.endpoint}")
    private String s3Endpoint;

    @Value("${spring.cloud.aws.credentials.access-key}")
    private String s3AccessKey;

    @Value("${spring.cloud.aws.credentials.secret-key}")
    private String s3SecretKey;

    @Value("${s3.bucket-name}")
    private String s3BucketName;

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

    private RegisterRequestDTO validRegisterEmployeeRequest(String suffix) {
        return new RegisterRequestDTO("EmpFirst" + suffix, "EmpLast" + suffix, "employee_" + suffix,
                "password123", "employee_" + suffix + "@example.com", Roles.EMPLOYEE,
                "+251911" + suffix.substring(0, 6));
    }

    private UserUpdateRequestDTO validUpdateEmployeeRequest(String suffix) {
        return new UserUpdateRequestDTO("UpdatedFirst" + suffix, "UpdatedLast" + suffix,
                "updated_employee_" + suffix, "password123", "updated_" + suffix + "@example.com",
                Roles.EMPLOYEE, "+251922" + suffix.substring(0, 6));
    }

    private ProductRequestDTO validProductRequest(String suffix, Set<UUID> categoryIds) {
        return ProductRequestDTO.builder().name("Product " + suffix).sku("SKU-" + suffix)
                .description("Description " + suffix).sellingPrice(new BigDecimal("100.00"))
                .costPrice(new BigDecimal("75.00")).quantity(50).lowStockThreshold(5)
                .categoryIds(categoryIds).build();
    }

    private ProductUpdateRequestDTO validProductUpdateRequest(String suffix,
            Set<UUID> categoryIds) {
        return ProductUpdateRequestDTO.builder().name("Updated Product " + suffix)
                .sku("UPD-SKU-" + suffix).description("Updated Description " + suffix)
                .sellingPrice(new BigDecimal("110.00")).costPrice(new BigDecimal("80.00"))
                .quantity(60).lowStockThreshold(8).active(true).categoryIds(categoryIds).build();
    }

    private CategoryRequestDTO validCategoryRequest(String suffix) {
        return CategoryRequestDTO.builder().name("Category " + suffix).build();
    }

    private CategoryUpdateRequestDTO validCategoryUpdateRequest(String suffix) {
        return CategoryUpdateRequestDTO.builder().name("Updated Category " + suffix).build();
    }

    private String randomSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private void putObjectInStorage(String objectKey) {
        try (S3Client s3Client =
                S3Client.builder().endpointOverride(URI.create(s3Endpoint))
                        .credentialsProvider(StaticCredentialsProvider
                                .create(AwsBasicCredentials.create(s3AccessKey, s3SecretKey)))
                        .region(Region.US_EAST_1)
                        .serviceConfiguration(
                                S3Configuration.builder().pathStyleAccessEnabled(true).build())
                        .build()) {
            s3Client.putObject(
                    PutObjectRequest.builder().bucket(s3BucketName).key(objectKey).build(),
                    RequestBody.fromString("image-content"));
        }
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

    private RegisterResponseDTO createEmployee(String accessToken, String suffix) {
        ResponseEntity<RegisterResponseDTO> createResponse =
                restTemplate.exchange(baseUrl() + "/employees", HttpMethod.POST,
                        authorizedJsonBody(validRegisterEmployeeRequest(suffix), accessToken),
                        RegisterResponseDTO.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().data()).isNotNull();
        return createResponse.getBody();
    }

    private ProductResponseDTO createProduct(String accessToken, String suffix) {
        ResponseEntity<ProductResponseDTO> createResponse =
                restTemplate.exchange(baseUrl() + "/products", HttpMethod.POST,
                        authorizedJsonBody(validProductRequest(suffix, null), accessToken),
                        ProductResponseDTO.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createResponse.getBody()).isNotNull();
        return createResponse.getBody();
    }

    private CategoryResponseDTO createCategory(String accessToken, String suffix) {
        ResponseEntity<CategoryResponseDTO> createResponse =
                restTemplate.exchange(baseUrl() + "/categories", HttpMethod.POST,
                        authorizedJsonBody(validCategoryRequest(suffix), accessToken),
                        CategoryResponseDTO.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createResponse.getBody()).isNotNull();
        return createResponse.getBody();
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

    @Test
    void shouldAllowAuthorizedRegisterEmployee() {
        String accessToken = loginAsAdminAccessToken();
        String suffix = randomSuffix();

        RegisterResponseDTO employee = createEmployee(accessToken, suffix);

        assertThat(employee.data().getEmail()).isEqualTo("employee_" + suffix + "@example.com");
    }

    @Test
    void shouldAllowAuthorizedGetAllEmployees() {
        String accessToken = loginAsAdminAccessToken();
        String suffix = randomSuffix();

        createEmployee(accessToken, suffix);

        ResponseEntity<UserResponseDTO[]> response = restTemplate.exchange(baseUrl() + "/employees",
                HttpMethod.GET, authorizedRequest(accessToken), UserResponseDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).anySatisfy(user -> assertThat(user.getEmail())
                .isEqualTo("employee_" + suffix + "@example.com"));
    }

    @Test
    void shouldAllowAuthorizedGetEmployeeById() {
        String accessToken = loginAsAdminAccessToken();
        String suffix = randomSuffix();
        UUID employeeId = createEmployee(accessToken, suffix).data().getId();

        ResponseEntity<UserResponseDTO> response =
                restTemplate.exchange(baseUrl() + "/employees/" + employeeId, HttpMethod.GET,
                        authorizedRequest(accessToken), UserResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(employeeId);
    }

    @Test
    void shouldAllowAuthorizedUpdateEmployee() {
        String accessToken = loginAsAdminAccessToken();
        String suffix = randomSuffix();
        UUID employeeId = createEmployee(accessToken, suffix).data().getId();

        ResponseEntity<UserResponseDTO> response =
                restTemplate.exchange(baseUrl() + "/employees/" + employeeId, HttpMethod.PUT,
                        authorizedJsonBody(validUpdateEmployeeRequest(suffix), accessToken),
                        UserResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("updated_" + suffix + "@example.com");
    }

    @Test
    void shouldAllowAuthorizedDeactivateEmployee() {
        String accessToken = loginAsAdminAccessToken();
        String suffix = randomSuffix();
        UUID employeeId = createEmployee(accessToken, suffix).data().getId();

        ResponseEntity<Void> response =
                restTemplate.exchange(baseUrl() + "/employees/" + employeeId + "/deactivate",
                        HttpMethod.POST, authorizedRequest(accessToken), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userRepository.findById(employeeId)).isPresent();
        assertThat(userRepository.findById(employeeId).orElseThrow().isActive()).isFalse();
    }

    @Test
    void shouldAllowAuthorizedActivateEmployee() {
        String accessToken = loginAsAdminAccessToken();
        String suffix = randomSuffix();
        UUID employeeId = createEmployee(accessToken, suffix).data().getId();

        ResponseEntity<Void> deactivateResponse =
                restTemplate.exchange(baseUrl() + "/employees/" + employeeId + "/deactivate",
                        HttpMethod.POST, authorizedRequest(accessToken), Void.class);
        assertThat(deactivateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Void> response =
                restTemplate.exchange(baseUrl() + "/employees/" + employeeId + "/activate",
                        HttpMethod.POST, authorizedRequest(accessToken), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userRepository.findById(employeeId)).isPresent();
        assertThat(userRepository.findById(employeeId).orElseThrow().isActive()).isTrue();
    }

    @Test
    void shouldAllowAuthorizedDeleteEmployee() {
        String accessToken = loginAsAdminAccessToken();
        String suffix = randomSuffix();
        UUID employeeId = createEmployee(accessToken, suffix).data().getId();

        ResponseEntity<Void> response =
                restTemplate.exchange(baseUrl() + "/employees/" + employeeId, HttpMethod.DELETE,
                        authorizedRequest(accessToken), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userRepository.existsById(employeeId)).isFalse();
    }

    @Test
    void shouldAllowAuthorizedCreateProduct() {
        String accessToken = loginAsAdminAccessToken();
        String suffix = randomSuffix();

        ProductResponseDTO product = createProduct(accessToken, suffix);

        assertThat(product.getName()).isEqualTo("Product " + suffix);
    }

    @Test
    void shouldAllowAuthorizedUpdateProduct() {
        String accessToken = loginAsAdminAccessToken();
        String suffix = randomSuffix();
        UUID productId = createProduct(accessToken, suffix).getId();

        ResponseEntity<ProductResponseDTO> response =
                restTemplate.exchange(baseUrl() + "/products/" + productId, HttpMethod.PUT,
                        authorizedJsonBody(validProductUpdateRequest(suffix, null), accessToken),
                        ProductResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Updated Product " + suffix);
    }

    @Test
    void shouldAllowAuthorizedDeleteProduct() {
        String accessToken = loginAsAdminAccessToken();
        String suffix = randomSuffix();
        UUID productId = createProduct(accessToken, suffix).getId();

        ResponseEntity<Void> response = restTemplate.exchange(baseUrl() + "/products/" + productId,
                HttpMethod.DELETE, authorizedRequest(accessToken), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(productRepository.existsById(productId)).isFalse();
    }

    @Test
    void shouldAllowAuthorizedActivateProduct() {
        String accessToken = loginAsAdminAccessToken();
        String suffix = randomSuffix();
        UUID productId = createProduct(accessToken, suffix).getId();

        ResponseEntity<Void> deactivateResponse =
                restTemplate.exchange(baseUrl() + "/products/" + productId + "/deactivate",
                        HttpMethod.POST, authorizedRequest(accessToken), Void.class);
        assertThat(deactivateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Void> response =
                restTemplate.exchange(baseUrl() + "/products/" + productId + "/activate",
                        HttpMethod.POST, authorizedRequest(accessToken), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(productRepository.findById(productId)).isPresent();
        assertThat(productRepository.findById(productId).orElseThrow().isActive()).isTrue();
    }

    @Test
    void shouldAllowAuthorizedDeactivateProduct() {
        String accessToken = loginAsAdminAccessToken();
        String suffix = randomSuffix();
        UUID productId = createProduct(accessToken, suffix).getId();

        ResponseEntity<Void> response =
                restTemplate.exchange(baseUrl() + "/products/" + productId + "/deactivate",
                        HttpMethod.POST, authorizedRequest(accessToken), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(productRepository.findById(productId)).isPresent();
        assertThat(productRepository.findById(productId).orElseThrow().isActive()).isFalse();
    }

    @Test
    void shouldAllowAuthorizedAddProductImage() {
        String accessToken = loginAsAdminAccessToken();
        String suffix = randomSuffix();
        UUID productId = createProduct(accessToken, suffix).getId();

        String objectKey = "product-image-" + suffix + ".png";
        putObjectInStorage(objectKey);

        ResponseEntity<ProductResponseDTO> response = restTemplate.exchange(
                baseUrl() + "/products/" + productId + "/images", HttpMethod.POST,
                authorizedJsonBody(new FileDTO(objectKey), accessToken), ProductResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getImageUrls()).isNotNull();
        assertThat(response.getBody().getImageUrls()).isNotEmpty();
    }

    @Test
    void shouldAllowAuthorizedCreateCategory() {
        String accessToken = loginAsAdminAccessToken();
        String suffix = randomSuffix();

        CategoryResponseDTO category = createCategory(accessToken, suffix);

        assertThat(category.getName()).isEqualTo("Category " + suffix);
    }

    @Test
    void shouldAllowAuthorizedUpdateCategory() {
        String accessToken = loginAsAdminAccessToken();
        String suffix = randomSuffix();
        UUID categoryId = createCategory(accessToken, suffix).getId();

        ResponseEntity<CategoryResponseDTO> response =
                restTemplate.exchange(baseUrl() + "/categories/" + categoryId, HttpMethod.PUT,
                        authorizedJsonBody(validCategoryUpdateRequest(suffix), accessToken),
                        CategoryResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Updated Category " + suffix);
    }

    @Test
    void shouldAllowAuthorizedDeleteCategory() {
        String accessToken = loginAsAdminAccessToken();
        String suffix = randomSuffix();
        UUID categoryId = createCategory(accessToken, suffix).getId();

        ResponseEntity<Void> response =
                restTemplate.exchange(baseUrl() + "/categories/" + categoryId, HttpMethod.DELETE,
                        authorizedRequest(accessToken), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(categoryRepository.existsById(categoryId)).isFalse();
    }
}
