package com.firomsa.inventory.v1.controller.integrationTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.firomsa.inventory.model.Product;
import com.firomsa.inventory.model.Role;
import com.firomsa.inventory.model.Restock;
import com.firomsa.inventory.model.Roles;
import com.firomsa.inventory.model.Sale;
import com.firomsa.inventory.model.User;
import com.firomsa.inventory.repository.ProductRepository;
import com.firomsa.inventory.repository.RestockRepository;
import com.firomsa.inventory.repository.RoleRepository;
import com.firomsa.inventory.repository.SaleRepository;
import com.firomsa.inventory.repository.UserRepository;

public class EmployeeControllerIntTest extends AbstractIntegrationTest {

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

    @Autowired
    private RestockRepository restockRepository;

    private static final String BASE_URL = "/api/v1/employee";

    @AfterEach
    void tearDown() {
        restockRepository.deleteAll();
        saleRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    private Product createProduct(String name, String sku) {
        return productRepository.save(Product.builder().name(name).quantity(50).sku(sku)
                .description("Product for employee sales").sellingPrice(BigDecimal.valueOf(15.0))
                .lowStockThreshold(5).costPrice(BigDecimal.valueOf(10.0)).build());
    }

    private Role employeeRole() {
        return roleRepository.findByName(Roles.EMPLOYEE).orElseThrow();
    }

    private User createEmployee(String email, String username) {
        return userRepository.save(User.builder().firstName("First").lastName("Last")
                .username(username).email(email).password("password").phone("0911223344")
                .role(employeeRole()).build());
    }

    private Sale createSale(User user, Product product, int quantity, BigDecimal salePrice) {
        return saleRepository.save(Sale.builder().soldBy(user).product(product).quantity(quantity)
                .salePrice(salePrice).build());
    }

    private Restock createRestock(User user, Product product, int quantity) {
        return restockRepository
                .save(Restock.builder().restockedBy(user).product(product).quantity(quantity).build());
    }

    @Test
    void shouldReturnUnauthorizedWhenMissingTokenForEmployeeSales() {
        assertThat(mockMvc.get().uri(BASE_URL + "/sales").exchange()).hasStatus(401);
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = "SCOPE_ADMIN")
    void shouldReturnForbiddenWhenAdminRequestsEmployeeSales() {
        assertThat(mockMvc.get().uri(BASE_URL + "/sales").exchange()).hasStatus(403);
    }

    @Test
    @WithMockUser(username = "employee.one@example.com", authorities = "SCOPE_EMPLOYEE")
    void shouldReturnOnlyAuthenticatedEmployeeSales() {
        var employeeOne = createEmployee("employee.one@example.com", "employee.one");
        var employeeTwo = createEmployee("employee.two@example.com", "employee.two");
        var productOne = createProduct("Product One", "SKU-EMP-1");
        var productTwo = createProduct("Product Two", "SKU-EMP-2");

        createSale(employeeOne, productOne, 3, BigDecimal.valueOf(20.0));
        createSale(employeeTwo, productTwo, 5, BigDecimal.valueOf(30.0));

        var result = mockMvc.get().uri(BASE_URL + "/sales").exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyText().contains(productOne.getId().toString());
        assertThat(result).bodyText().doesNotContain(productTwo.getId().toString());
    }

    @Test
    void shouldReturnUnauthorizedWhenMissingTokenForEmployeeRestocks() {
        assertThat(mockMvc.get().uri(BASE_URL + "/restocks").exchange()).hasStatus(401);
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = "SCOPE_ADMIN")
    void shouldReturnForbiddenWhenAdminRequestsEmployeeRestocks() {
        assertThat(mockMvc.get().uri(BASE_URL + "/restocks").exchange()).hasStatus(403);
    }

    @Test
    @WithMockUser(username = "employee.one@example.com", authorities = "SCOPE_EMPLOYEE")
    void shouldReturnOnlyAuthenticatedEmployeeRestocks() {
        var employeeOne = createEmployee("employee.one@example.com", "employee.one");
        var employeeTwo = createEmployee("employee.two@example.com", "employee.two");
        var productOne = createProduct("Restock Product One", "SKU-RESTOCK-1");
        var productTwo = createProduct("Restock Product Two", "SKU-RESTOCK-2");

        createRestock(employeeOne, productOne, 8);
        createRestock(employeeTwo, productTwo, 10);

        var result = mockMvc.get().uri(BASE_URL + "/restocks").exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyText().contains(productOne.getId().toString());
        assertThat(result).bodyText().doesNotContain(productTwo.getId().toString());
    }
}
