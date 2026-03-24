package com.firomsa.inventory.v1.controller.integrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.UUID;
import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.firomsa.inventory.repository.CategoryRepository;
import com.firomsa.inventory.repository.ConfirmationOtpRepository;
import com.firomsa.inventory.repository.ProductRepository;
import com.firomsa.inventory.repository.RoleRepository;
import com.firomsa.inventory.repository.SaleRepository;
import com.firomsa.inventory.repository.UserRepository;
import com.firomsa.inventory.model.Product;
import com.firomsa.inventory.model.Role;
import com.firomsa.inventory.model.Roles;
import com.firomsa.inventory.model.Sale;
import com.firomsa.inventory.model.User;

public class AdminControllerIntTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private ConfirmationOtpRepository confirmationOtpRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SaleRepository saleRepository;

    private static final String BASE_URL = "/api/v1/admin";

    @AfterEach
    void tearDown() {
        saleRepository.deleteAll();
        confirmationOtpRepository.deleteAll();
        userRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    private String randomSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private String registerEmployeeJson(String suffix) {
        return """
                {
                    "firstName": "EmpFirst%s",
                    "lastName": "EmpLast%s",
                    "username": "employee_%s",
                    "password": "password123",
                    "email": "employee_%s@example.com",
                    "role": "EMPLOYEE",
                    "phone": "+251911%s"
                }
                """.formatted(suffix, suffix, suffix, suffix, suffix.substring(0, 6));
    }

    private String updateEmployeeJson(String suffix) {
        return """
                {
                    "firstName": "UpdatedFirst%s",
                    "lastName": "UpdatedLast%s",
                    "username": "updated_employee_%s",
                    "password": "password123",
                    "email": "updated_%s@example.com",
                    "role": "EMPLOYEE",
                    "phone": "+251922%s"
                }
                """.formatted(suffix, suffix, suffix, suffix, suffix.substring(0, 6));
    }

    private String productJson(String suffix) {
        return """
                {
                    "name": "Product %s",
                    "sku": "SKU-%s",
                    "description": "Description %s",
                    "sellingPrice": 100.00,
                    "costPrice": 75.00,
                    "quantity": 50,
                    "lowStockThreshold": 5
                }
                """.formatted(suffix, suffix, suffix);
    }

    private String updateProductJson(String suffix) {
        return """
                {
                    "name": "Updated Product %s",
                    "sku": "UPD-SKU-%s",
                    "description": "Updated Description %s",
                    "sellingPrice": 110.00,
                    "costPrice": 80.00,
                    "quantity": 60,
                    "lowStockThreshold": 8,
                    "active": true
                }
                """.formatted(suffix, suffix, suffix);
    }

    private String categoryJson(String suffix) {
        return """
                {
                    "name": "Category %s"
                }
                """.formatted(suffix);
    }

    private String updateCategoryJson(String suffix) {
        return """
                {
                    "name": "Updated Category %s"
                }
                """.formatted(suffix);
    }

    private UUID findEmployeeIdBySuffix(String suffix) {
        return userRepository.findByEmail("employee_" + suffix + "@example.com").orElseThrow()
                .getId();
    }

    private UUID findProductIdBySuffix(String suffix) {
        return productRepository.findAll().stream()
                .filter(p -> ("Product " + suffix).equals(p.getName())).findFirst().orElseThrow()
                .getId();
    }

    private UUID findCategoryIdBySuffix(String suffix) {
        return categoryRepository.findAll().stream()
                .filter(c -> ("Category " + suffix).equals(c.getName())).findFirst().orElseThrow()
                .getId();
    }

    private void createEmployee(String suffix) {
        MvcTestResult result = mockMvc.post().uri(BASE_URL + "/employees")
                .contentType(APPLICATION_JSON).content(registerEmployeeJson(suffix)).exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.data.email").asString()
                .isEqualTo("employee_" + suffix + "@example.com");
    }

    private void createProduct(String suffix) {
        MvcTestResult result = mockMvc.post().uri(BASE_URL + "/products")
                .contentType(APPLICATION_JSON).content(productJson(suffix)).exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.name").asString()
                .isEqualTo("Product " + suffix);
    }

    private void createCategory(String suffix) {
        MvcTestResult result = mockMvc.post().uri(BASE_URL + "/categories")
                .contentType(APPLICATION_JSON).content(categoryJson(suffix)).exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.name").asString()
                .isEqualTo("Category " + suffix);
    }

    private Role employeeRole() {
        return roleRepository.findByName(Roles.EMPLOYEE).orElseThrow();
    }

    private User createEmployeeUser(String suffix) {
        return userRepository.save(User.builder().firstName("John").lastName("Doe")
                .username("employee.sales." + suffix).email("employee.sales." + suffix + "@example.com")
                .password("password").phone("1234567").role(employeeRole()).build());
    }

    private Product createProductForSale(String suffix) {
        return productRepository.save(Product.builder().name("Sales Product " + suffix)
                .quantity(40).sku("SALES-SKU-" + suffix)
                .description("Sales listing integration product")
                .sellingPrice(BigDecimal.valueOf(20.0)).lowStockThreshold(5)
                .costPrice(BigDecimal.valueOf(10.0)).build());
    }

    private Sale createSale(User user, Product product, int quantity, double salePrice) {
        return saleRepository.save(Sale.builder().soldBy(user).product(product).quantity(quantity)
                .salePrice(salePrice).build());
    }

    @Test
    void shouldRejectUnauthorizedRegisterEmployee() {
        assertThat(mockMvc.post().uri(BASE_URL + "/employees").contentType(APPLICATION_JSON)
                .content("{}").exchange()).hasStatus(401);
    }

    @Test
    void shouldRejectUnauthorizedGetAllEmployees() {
        assertThat(mockMvc.get().uri(BASE_URL + "/employees").exchange()).hasStatus(401);
    }

    @Test
    void shouldRejectUnauthorizedGetEmployeeById() {
        assertThat(mockMvc.get().uri(BASE_URL + "/employees/{id}", UUID.randomUUID()).exchange())
                .hasStatus(401);
    }

    @Test
    void shouldRejectUnauthorizedUpdateEmployee() {
        assertThat(mockMvc.put().uri(BASE_URL + "/employees/{id}", UUID.randomUUID())
                .contentType(APPLICATION_JSON).content("{}").exchange()).hasStatus(401);
    }

    @Test
    void shouldRejectUnauthorizedDeactivateEmployee() {
        assertThat(mockMvc.post().uri(BASE_URL + "/employees/{id}/deactivate", UUID.randomUUID())
                .exchange()).hasStatus(401);
    }

    @Test
    void shouldRejectUnauthorizedActivateEmployee() {
        assertThat(mockMvc.post().uri(BASE_URL + "/employees/{id}/activate", UUID.randomUUID())
                .exchange()).hasStatus(401);
    }

    @Test
    void shouldRejectUnauthorizedDeleteEmployee() {
        assertThat(mockMvc.delete().uri(BASE_URL + "/employees/{id}", UUID.randomUUID()).exchange())
                .hasStatus(401);
    }

    @Test
    void shouldRejectUnauthorizedCreateProduct() {
        assertThat(mockMvc.post().uri(BASE_URL + "/products").contentType(APPLICATION_JSON)
                .content("{}").exchange()).hasStatus(401);
    }

    @Test
    void shouldRejectUnauthorizedUpdateProduct() {
        assertThat(mockMvc.put().uri(BASE_URL + "/products/{id}", UUID.randomUUID())
                .contentType(APPLICATION_JSON).content("{}").exchange()).hasStatus(401);
    }

    @Test
    void shouldRejectUnauthorizedDeleteProduct() {
        assertThat(mockMvc.delete().uri(BASE_URL + "/products/{id}", UUID.randomUUID()).exchange())
                .hasStatus(401);
    }

    @Test
    void shouldRejectUnauthorizedActivateProduct() {
        assertThat(mockMvc.post().uri(BASE_URL + "/products/{id}/activate", UUID.randomUUID())
                .exchange()).hasStatus(401);
    }

    @Test
    void shouldRejectUnauthorizedDeactivateProduct() {
        assertThat(mockMvc.post().uri(BASE_URL + "/products/{id}/deactivate", UUID.randomUUID())
                .exchange()).hasStatus(401);
    }

    @Test
    void shouldRejectUnauthorizedAddProductImage() {
        assertThat(mockMvc.post().uri(BASE_URL + "/products/{id}/images", UUID.randomUUID())
                .contentType(APPLICATION_JSON).content("{}").exchange()).hasStatus(401);
    }

    @Test
    void shouldRejectUnauthorizedCreateCategory() {
        assertThat(mockMvc.post().uri(BASE_URL + "/categories").contentType(APPLICATION_JSON)
                .content("{}").exchange()).hasStatus(401);
    }

    @Test
    void shouldRejectUnauthorizedUpdateCategory() {
        assertThat(mockMvc.put().uri(BASE_URL + "/categories/{id}", UUID.randomUUID())
                .contentType(APPLICATION_JSON).content("{}").exchange()).hasStatus(401);
    }

    @Test
    void shouldRejectUnauthorizedDeleteCategory() {
        assertThat(
                mockMvc.delete().uri(BASE_URL + "/categories/{id}", UUID.randomUUID()).exchange())
                .hasStatus(401);
    }

    @Test
    void shouldRejectUnauthorizedGetAllSales() {
        assertThat(mockMvc.get().uri(BASE_URL + "/sales").exchange()).hasStatus(401);
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldReturnBadRequestWhenCreateEmployeePayloadIsInvalid() {
        assertThat(mockMvc.post().uri(BASE_URL + "/employees").contentType(APPLICATION_JSON)
                .content("{}").exchange()).hasStatus(400);
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldReturnBadRequestWhenCreateProductPayloadIsInvalid() {
        assertThat(mockMvc.post().uri(BASE_URL + "/products").contentType(APPLICATION_JSON)
                .content("{}").exchange()).hasStatus(400);
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldReturnBadRequestWhenCreateCategoryPayloadIsInvalid() {
        assertThat(mockMvc.post().uri(BASE_URL + "/categories").contentType(APPLICATION_JSON)
                .content("{}").exchange()).hasStatus(400);
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldReturnBadRequestWhenEmployeeIdIsMalformed() {
        assertThat(mockMvc.get().uri(BASE_URL + "/employees/not-a-uuid").exchange()).hasStatus(400);
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldAllowAuthorizedGetAllSales() {
        String suffix = randomSuffix();
        var employee = createEmployeeUser(suffix);
        var product = createProductForSale(suffix);
        createSale(employee, product, 3, 18.5);

        var result = mockMvc.get().uri(BASE_URL + "/sales").exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyText().contains(product.getId().toString());
        assertThat(result).bodyText().contains("\"quantity\":3");
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldAllowAuthorizedRegisterEmployee() {
        String suffix = randomSuffix();
        createEmployee(suffix);
        assertThat(userRepository.findByEmail("employee_" + suffix + "@example.com")).isPresent();
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldAllowAuthorizedGetAllEmployees() {
        String suffix = randomSuffix();
        createEmployee(suffix);

        MvcTestResult result = mockMvc.get().uri(BASE_URL + "/employees").exchange();
        assertThat(result).hasStatusOk();
        assertThat(result).bodyText().contains("employee_" + suffix + "@example.com");
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldAllowAuthorizedGetEmployeeById() {
        String suffix = randomSuffix();
        createEmployee(suffix);
        UUID employeeId = findEmployeeIdBySuffix(suffix);

        MvcTestResult result = mockMvc.get().uri(BASE_URL + "/employees/{id}", employeeId).exchange();
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.id").asString()
                .isEqualTo(employeeId.toString());
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldAllowAuthorizedUpdateEmployee() {
        String suffix = randomSuffix();
        createEmployee(suffix);
        UUID employeeId = findEmployeeIdBySuffix(suffix);

        MvcTestResult result = mockMvc.put().uri(BASE_URL + "/employees/{id}", employeeId)
                .contentType(APPLICATION_JSON).content(updateEmployeeJson(suffix)).exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.email").asString()
                .isEqualTo("updated_" + suffix + "@example.com");
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldAllowAuthorizedDeactivateEmployee() {
        String suffix = randomSuffix();
        createEmployee(suffix);
        UUID employeeId = findEmployeeIdBySuffix(suffix);

        assertThat(
                mockMvc.post().uri(BASE_URL + "/employees/{id}/deactivate", employeeId).exchange())
                .hasStatusOk();

        assertThat(userRepository.findById(employeeId)).isPresent();
        assertThat(userRepository.findById(employeeId).orElseThrow().isActive()).isFalse();
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldAllowAuthorizedActivateEmployee() {
        String suffix = randomSuffix();
        createEmployee(suffix);
        UUID employeeId = findEmployeeIdBySuffix(suffix);

        assertThat(
                mockMvc.post().uri(BASE_URL + "/employees/{id}/deactivate", employeeId).exchange())
                .hasStatusOk();

        assertThat(mockMvc.post().uri(BASE_URL + "/employees/{id}/activate", employeeId).exchange())
                .hasStatusOk();

        assertThat(userRepository.findById(employeeId)).isPresent();
        assertThat(userRepository.findById(employeeId).orElseThrow().isActive()).isTrue();
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldAllowAuthorizedDeleteEmployee() {
        String suffix = randomSuffix();
        createEmployee(suffix);
        UUID employeeId = findEmployeeIdBySuffix(suffix);

        assertThat(mockMvc.delete().uri(BASE_URL + "/employees/{id}", employeeId).exchange())
                .hasStatusOk();

        assertThat(userRepository.existsById(employeeId)).isFalse();
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldAllowAuthorizedCreateProduct() {
        String suffix = randomSuffix();
        createProduct(suffix);
        assertThat(productRepository.findAll())
                .anySatisfy(p -> assertThat(p.getName()).isEqualTo("Product " + suffix));
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldAllowAuthorizedUpdateProduct() {
        String suffix = randomSuffix();
        createProduct(suffix);
        UUID productId = findProductIdBySuffix(suffix);

        MvcTestResult result = mockMvc.put().uri(BASE_URL + "/products/{id}", productId)
                .contentType(APPLICATION_JSON).content(updateProductJson(suffix)).exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.name").asString()
                .isEqualTo("Updated Product " + suffix);
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldAllowAuthorizedDeleteProduct() {
        String suffix = randomSuffix();
        createProduct(suffix);
        UUID productId = findProductIdBySuffix(suffix);

        assertThat(mockMvc.delete().uri(BASE_URL + "/products/{id}", productId).exchange())
                .hasStatusOk();

        assertThat(productRepository.existsById(productId)).isFalse();
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldAllowAuthorizedActivateProduct() {
        String suffix = randomSuffix();
        createProduct(suffix);
        UUID productId = findProductIdBySuffix(suffix);

        assertThat(mockMvc.post().uri(BASE_URL + "/products/{id}/deactivate", productId).exchange())
                .hasStatusOk();

        assertThat(mockMvc.post().uri(BASE_URL + "/products/{id}/activate", productId).exchange())
                .hasStatusOk();

        assertThat(productRepository.findById(productId)).isPresent();
        assertThat(productRepository.findById(productId).orElseThrow().isActive()).isTrue();
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldAllowAuthorizedDeactivateProduct() {
        String suffix = randomSuffix();
        createProduct(suffix);
        UUID productId = findProductIdBySuffix(suffix);

        assertThat(mockMvc.post().uri(BASE_URL + "/products/{id}/deactivate", productId).exchange())
                .hasStatusOk();

        assertThat(productRepository.findById(productId)).isPresent();
        assertThat(productRepository.findById(productId).orElseThrow().isActive()).isFalse();
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldAllowAuthorizedAddProductImage() {
        String suffix = randomSuffix();
        createProduct(suffix);
        UUID productId = findProductIdBySuffix(suffix);

        String objectKey = "product-image-" + suffix + ".png";
        when(storageService.exists(objectKey)).thenReturn(true);
        when(storageService.getUrl(objectKey)).thenReturn("https://example.com/" + objectKey);

        MvcTestResult result = mockMvc.post().uri(BASE_URL + "/products/{id}/images", productId)
                .contentType(APPLICATION_JSON).content("{\"objectKey\":\"" + objectKey + "\"}")
                .exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().hasPath("$.imageUrls[0]");
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldAllowAuthorizedCreateCategory() {
        String suffix = randomSuffix();
        createCategory(suffix);
        assertThat(categoryRepository.findAll())
                .anySatisfy(c -> assertThat(c.getName()).isEqualTo("Category " + suffix));
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldAllowAuthorizedUpdateCategory() {
        String suffix = randomSuffix();
        createCategory(suffix);
        UUID categoryId = findCategoryIdBySuffix(suffix);

        MvcTestResult result = mockMvc.put().uri(BASE_URL + "/categories/{id}", categoryId)
                .contentType(APPLICATION_JSON).content(updateCategoryJson(suffix)).exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.name").asString()
                .isEqualTo("Updated Category " + suffix);
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void shouldAllowAuthorizedDeleteCategory() {
        String suffix = randomSuffix();
        createCategory(suffix);
        UUID categoryId = findCategoryIdBySuffix(suffix);

        assertThat(mockMvc.delete().uri(BASE_URL + "/categories/{id}", categoryId).exchange())
                .hasStatusOk();

        assertThat(categoryRepository.existsById(categoryId)).isFalse();
    }
}
