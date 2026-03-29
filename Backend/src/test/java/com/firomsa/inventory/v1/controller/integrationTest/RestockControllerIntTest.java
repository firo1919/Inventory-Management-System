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
import com.firomsa.inventory.model.Restock;
import com.firomsa.inventory.model.Role;
import com.firomsa.inventory.model.Roles;
import com.firomsa.inventory.model.User;
import com.firomsa.inventory.repository.ProductRepository;
import com.firomsa.inventory.repository.RestockRepository;
import com.firomsa.inventory.repository.RoleRepository;
import com.firomsa.inventory.repository.UserRepository;
import com.firomsa.inventory.v1.dto.RestockRequestDTO;

import tools.jackson.databind.ObjectMapper;

public class RestockControllerIntTest extends AbstractIntegrationTest {
    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RestockRepository restockRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BASE_URL = "/api/v1/restocks";

    @AfterEach
    void tearDown() {
        restockRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    Product getProduct() {
        return productRepository.save(Product.builder().name("Restock Product").quantity(10).sku("restock-id-222")
                .description("Test restock product description")
                .sellingPrice(BigDecimal.valueOf(10.0)).lowStockThreshold(5)
                .costPrice(BigDecimal.valueOf(15)).build());
    }

    Role getRole() {
        return roleRepository.findByName(Roles.EMPLOYEE).orElseThrow();
    }

    User getUser() {
        return userRepository.save(User.builder().firstName("joe").lastName("doe")
                .username("joedoe").email("user@example.com").password("password")
                .phone("1234567").role(getRole()).build());
    }

    Restock createRestock(Product product, User user, int quantity) {
        return restockRepository.save(Restock.builder().product(product).restockedBy(user)
                .quantity(quantity).build());
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = { "SCOPE_EMPLOYEE", "SCOPE_ADMIN" })
    void shouldCreateARestock() {
        var product = getProduct();
        getUser();
        var request = RestockRequestDTO.builder().productId(product.getId()).quantity(5).build();

        var result = mockMvc.post().uri(BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)).exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.message")
                .isEqualTo("Restock recorded successfully");
        assertThat(result).bodyJson().extractingPath("$.quantity")
                .isEqualTo(request.getQuantity());
        assertThat(result).bodyJson().extractingPath("$.productId")
                .isEqualTo(request.getProductId().toString());
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = { "SCOPE_EMPLOYEE", "SCOPE_ADMIN" })
    void shouldThrowExceptionWhenProductNotFound() {
        getUser();
        var request = RestockRequestDTO.builder().productId(UUID.randomUUID()).quantity(5).build();

        var result = mockMvc.post().uri(BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)).exchange();

        assertThat(result).hasStatus(404);
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo(404);
        assertThat(result).bodyJson().extractingPath("$.message")
                .isEqualTo("Resource not found, Product not found with id: " + request.getProductId());
    }

    @Test
    void shouldReturnUnauthorizedWhenUserNotAuthenticated() {
        var product = getProduct();
        var request = RestockRequestDTO.builder().productId(product.getId()).quantity(5).build();

        var result = mockMvc.post().uri(BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)).exchange();

        assertThat(result).hasStatus(401);
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = { "SCOPE_EMPLOYEE", "SCOPE_ADMIN" })
    void shouldGetRestockById() {
        var product = getProduct();
        var user = getUser();
        var restock = createRestock(product, user, 4);

        var result = mockMvc.get().uri(BASE_URL + "/{id}", restock.getId()).exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.productId")
                .isEqualTo(product.getId().toString());
        assertThat(result).bodyJson().extractingPath("$.quantity").isEqualTo(4);
    }

    @Test
    @WithMockUser(username = "user@example.com", authorities = { "SCOPE_EMPLOYEE", "SCOPE_ADMIN" })
    void shouldReturnNotFoundWhenRestockIdDoesNotExist() {
        getUser();

        var result = mockMvc.get().uri(BASE_URL + "/{id}", UUID.randomUUID()).exchange();

        assertThat(result).hasStatus(404);
        assertThat(result).bodyText().contains("Restock not found with id");
    }

    @Test
    void shouldReturnUnauthorizedWhenGetRestockByIdAndUserNotAuthenticated() {
        var result = mockMvc.get().uri(BASE_URL + "/{id}", UUID.randomUUID()).exchange();

        assertThat(result).hasStatus(401);
    }

}
