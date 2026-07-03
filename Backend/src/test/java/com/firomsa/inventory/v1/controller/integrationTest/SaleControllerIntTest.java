package com.firomsa.inventory.v1.controller.integrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.firomsa.inventory.model.Product;
import com.firomsa.inventory.model.Role;
import com.firomsa.inventory.model.Roles;
import com.firomsa.inventory.model.Sale;
import com.firomsa.inventory.model.User;
import com.firomsa.inventory.repository.ProductRepository;
import com.firomsa.inventory.repository.RoleRepository;
import com.firomsa.inventory.repository.SaleRepository;
import com.firomsa.inventory.repository.UserRepository;
import com.firomsa.inventory.v1.dto.SaleRequestDTO;

import tools.jackson.databind.ObjectMapper;

public class SaleControllerIntTest extends AbstractIntegrationTest {
    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SaleRepository saleRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BASE_URL = "/api/v1/sales";

    @AfterEach
    void tearDown() {
        saleRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    Product getProduct() {
        return productRepository.save(Product.builder().name("Test Product").quantity(10).sku("id-222")
                .description("Test product description").sellingPrice(BigDecimal.valueOf(10.0)).lowStockThreshold(5)
                .costPrice(BigDecimal.valueOf(15))
                .build());
    }

    Role getRole() {
        return roleRepository.findByName(Roles.EMPLOYEE).get();
    }

    User getUser() {
        return userRepository.save(
                User.builder().firstName("joe").lastName("doe").username("joedoe").email("user@example.com")
                        .password("password").phone("1234567").role(getRole()).build());
    }

    Sale createSale(Product product, User user, int quantity, double salePrice) {
        return saleRepository.save(Sale.builder().product(product).soldBy(user).quantity(quantity)
                .salePrice(salePrice).build());
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = { "SCOPE_EMPLOYEE", "SCOPE_ADMIN" })
    void shouldCreateASale() {
        // Arrange
        var product = getProduct();
        getUser();
        var request = SaleRequestDTO.builder().productId(product.getId()).quantity(5)
                .salePrice(10.0).build();

        // Act
        var result = mockMvc.post().uri(BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)).exchange();

        // Assert
        assertThat(result).hasStatus(org.springframework.http.HttpStatus.CREATED);
        assertThat(result).bodyJson()
                .extractingPath("$.message").isEqualTo("Sale recorded successfully");
        assertThat(result).bodyJson()
                .extractingPath("$.quantity").isEqualTo(request.getQuantity());
        assertThat(result).bodyJson()
                .extractingPath("$.salePrice").isEqualTo(request.getSalePrice());
        assertThat(result).bodyJson()
                .extractingPath("$.productId").isEqualTo(request.getProductId()
                        .toString());
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = { "SCOPE_EMPLOYEE", "SCOPE_ADMIN" })
    void shouldThrowExceptionWhenProductNotFound() {
        // Arrange
        getUser();
        var request = SaleRequestDTO.builder().productId(UUID.randomUUID()).quantity(5)
                .salePrice(10.0).build();

        // Act
        var result = mockMvc.post().uri(BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)).exchange();

        // Act & Assert
        assertThat(result).hasStatus(404);
        assertThat(result).bodyJson()
                .extractingPath("$.status").isEqualTo(404);
        assertThat(result).bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Resource not found, Product not found with id: " + request.getProductId());
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = { "SCOPE_EMPLOYEE", "SCOPE_ADMIN" })
    void shouldThrowExceptionWhenInsufficientStock() {
        // Arrange
        var product = getProduct();
        getUser();
        var request = SaleRequestDTO.builder().productId(product.getId()).quantity(15)
                .salePrice(10.0).build();

        // Act
        var result = mockMvc.post().uri(BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)).exchange();

        // Assert
        assertThat(result).hasStatus(400);
        assertThat(result).bodyJson()
                .extractingPath("$.status").isEqualTo(400);
        assertThat(result).bodyJson()
                .extractingPath("$.message").isEqualTo("Insufficient stock for product: " + product.getName());
    }

    @Test
    void shouldReturnForbiddenWhenUserNotAuthenticated() {
        // Arrange
        var product = getProduct();
        var request = SaleRequestDTO.builder().productId(product.getId()).quantity(5)
                .salePrice(10.0).build();
        // Act
        var result = mockMvc.post().uri(BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)).exchange();
        // Assert
        assertThat(result).hasStatus(401);
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = { "SCOPE_EMPLOYEE", "SCOPE_ADMIN" })
    void shouldGetSaleById() {
        // Arrange
        var product = getProduct();
        var user = getUser();
        var sale = createSale(product, user, 4, 13.5);

        // Act
        var result = mockMvc.get().uri(BASE_URL + "/{id}", sale.getId()).exchange();

        // Assert
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.productId")
                .isEqualTo(product.getId().toString());
        assertThat(result).bodyJson().extractingPath("$.quantity").isEqualTo(4);
        assertThat(result).bodyJson().extractingPath("$.salePrice").isEqualTo(13.5);
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = { "SCOPE_EMPLOYEE", "SCOPE_ADMIN" })
    void shouldReturnNotFoundWhenSaleIdDoesNotExist() {
        // Arrange
        getUser();

        // Act
        var result = mockMvc.get().uri(BASE_URL + "/{id}", UUID.randomUUID()).exchange();

        // Assert
        assertThat(result).hasStatus(404);
        assertThat(result).bodyText().contains("Sale not found with id");
    }

    @Test
    void shouldReturnUnauthorizedWhenGetSaleByIdAndUserNotAuthenticated() {
        // Act
        var result = mockMvc.get().uri(BASE_URL + "/{id}", UUID.randomUUID()).exchange();

        // Assert
        assertThat(result).hasStatus(401);
    }

}
