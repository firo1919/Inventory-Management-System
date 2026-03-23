package com.firomsa.inventory.v1.controller.unitTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.firomsa.inventory.model.Roles;
import com.firomsa.inventory.v1.controller.AdminController;
import com.firomsa.inventory.v1.dto.CategoryRequestDTO;
import com.firomsa.inventory.v1.dto.CategoryResponseDTO;
import com.firomsa.inventory.v1.dto.CategoryUpdateRequestDTO;
import com.firomsa.inventory.v1.dto.ProductRequestDTO;
import com.firomsa.inventory.v1.dto.ProductResponseDTO;
import com.firomsa.inventory.v1.dto.ProductUpdateRequestDTO;
import com.firomsa.inventory.v1.dto.RegisterRequestDTO;
import com.firomsa.inventory.v1.dto.RegisterResponseDTO;
import com.firomsa.inventory.v1.dto.UserResponseDTO;
import com.firomsa.inventory.v1.dto.UserUpdateRequestDTO;
import com.firomsa.inventory.v1.service.AuthService;
import com.firomsa.inventory.v1.service.CategoryService;
import com.firomsa.inventory.v1.service.EmployeeService;
import com.firomsa.inventory.v1.service.ProductService;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "SCOPE_ADMIN")
public class AdminControllerUnitTest {

    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private EmployeeService employeeService;
    @MockitoBean
    private ProductService productService;
    @MockitoBean
    private CategoryService categoryService;

    @Autowired
    private MockMvcTester mockMvc;

    private static final String BASE_URL = "/api/v1/admin";

    private UserResponseDTO sampleUser(UUID id, String username) {
        return new UserResponseDTO(id, "John", "Doe", username, username + "@example.com",
                "+251911111111", Roles.EMPLOYEE.name(), null, "2026-03-19T10:15:30", true, true);
    }

    private ProductResponseDTO sampleProduct(UUID id, String name) {
        return ProductResponseDTO.builder().id(id).name(name).sku("SKU-001")
                .description("Sample product").sellingPrice(new BigDecimal("100.00"))
                .costPrice(new BigDecimal("80.00")).quantity(20).lowStockThreshold(2).active(true)
                .createdAt(LocalDateTime.of(2026, 3, 19, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 3, 19, 10, 30)).categoryIds(Set.of())
                .imageUrls(List.of("https://cdn.example.com/image-1.jpg")).build();
    }

    private CategoryResponseDTO sampleCategory(UUID id, String name) {
        return CategoryResponseDTO.builder().id(id).name(name)
                .createdAt(LocalDateTime.of(2026, 3, 19, 8, 0))
                .updatedAt(LocalDateTime.of(2026, 3, 19, 9, 0)).productIds(Set.of()).build();
    }

    private String registerRequestJson() {
        return """
                {
                    "firstName": "Jane",
                    "lastName": "Smith",
                    "username": "jane.smith",
                    "password": "password123",
                    "email": "jane.smith@example.com",
                    "role": "EMPLOYEE",
                    "phone": "+251922222222"
                }
                """;
    }

    private String productRequestJson() {
        return """
                {
                    "name": "Coffee",
                    "sku": "COF-1",
                    "description": "Ground coffee",
                    "sellingPrice": 12.50,
                    "costPrice": 9.00,
                    "quantity": 50,
                    "lowStockThreshold": 5
                }
                """;
    }

    private String productUpdateRequestJson() {
        return """
                {
                    "name": "Coffee Premium",
                    "sellingPrice": 14.50,
                    "quantity": 45,
                    "active": true
                }
                """;
    }

    private String categoryRequestJson(String name) {
        return "{\"name\":\"" + name + "\"}";
    }

    private String fileDtoJson(String objectKey) {
        return "{\"objectKey\":\"" + objectKey + "\"}";
    }

    @Test
    void shouldReturnAllEmployees() {
        UserResponseDTO user = sampleUser(UUID.randomUUID(), "employee.one");
        when(employeeService.getEmployees()).thenReturn(List.of(user));

        MvcTestResult result = mockMvc.get().uri(BASE_URL + "/employees").exchange();
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$[0].id").asString()
                .isEqualTo(user.getId().toString());
        assertThat(result).bodyJson().extractingPath("$[0].username").asString()
                .isEqualTo("employee.one");

        verify(employeeService).getEmployees();
    }

    @Test
    void shouldActivateProduct() {
        UUID productId = UUID.randomUUID();
        doNothing().when(productService).activate(productId);

        MvcTestResult result = mockMvc.post().uri(BASE_URL + "/products/{id}/activate", productId)
                .with(csrf()).exchange();
        assertThat(result).hasStatusOk();

        verify(productService).activate(productId);
    }

    @Test
    void shouldAddProductImage() {
        UUID productId = UUID.randomUUID();
        String objectKey = "products/image-1.jpg";
        ProductResponseDTO response = sampleProduct(productId, "Coffee");

        when(productService.addImageToProduct(productId, objectKey)).thenReturn(response);

        MvcTestResult result = mockMvc.post().uri(BASE_URL + "/products/{id}/images", productId).with(csrf())
                .contentType(APPLICATION_JSON).content(fileDtoJson(objectKey)).exchange();
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.id").asString()
                .isEqualTo(productId.toString());
        assertThat(result).bodyJson().extractingPath("$.imageUrls[0]").asString()
                .isEqualTo("https://cdn.example.com/image-1.jpg");

        verify(productService).addImageToProduct(productId, objectKey);
    }

    @Test
    void shouldCreateCategory() {
        CategoryResponseDTO response = sampleCategory(UUID.randomUUID(), "Beverages");
        when(categoryService.create(any(CategoryRequestDTO.class))).thenReturn(response);

        MvcTestResult result = mockMvc.post().uri(BASE_URL + "/categories").with(csrf())
                .contentType(APPLICATION_JSON).content(categoryRequestJson("Beverages")).exchange();
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.id").asString()
                .isEqualTo(response.getId().toString());
        assertThat(result).bodyJson().extractingPath("$.name").asString().isEqualTo("Beverages");

        verify(categoryService).create(any(CategoryRequestDTO.class));
    }

    @Test
    void shouldCreateProduct() {
        ProductResponseDTO response = sampleProduct(UUID.randomUUID(), "Coffee");
        when(productService.create(any(ProductRequestDTO.class))).thenReturn(response);

        MvcTestResult result = mockMvc.post().uri(BASE_URL + "/products").with(csrf())
                .contentType(APPLICATION_JSON).content(productRequestJson()).exchange();
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.id").asString()
                .isEqualTo(response.getId().toString());
        assertThat(result).bodyJson().extractingPath("$.name").asString().isEqualTo("Coffee");

        verify(productService).create(any(ProductRequestDTO.class));
    }

    @Test
    void shouldDeactivateEmployee() {
        UUID employeeId = UUID.randomUUID();
        doNothing().when(employeeService).deactivateEmployee(employeeId);

        MvcTestResult result = mockMvc.post()
                .uri(BASE_URL + "/employees/{id}/deactivate", employeeId).with(csrf()).exchange();
        assertThat(result).hasStatusOk();

        verify(employeeService).deactivateEmployee(employeeId);
    }

    @Test
    void shouldDeactivateProduct() {
        UUID productId = UUID.randomUUID();
        doNothing().when(productService).deactivate(productId);

        MvcTestResult result = mockMvc.post().uri(BASE_URL + "/products/{id}/deactivate", productId)
                .with(csrf()).exchange();
        assertThat(result).hasStatusOk();

        verify(productService).deactivate(productId);
    }

    @Test
    void shouldDeleteCategory() {
        UUID categoryId = UUID.randomUUID();
        doNothing().when(categoryService).delete(categoryId);

        MvcTestResult result = mockMvc.delete().uri(BASE_URL + "/categories/{id}", categoryId)
                .with(csrf()).exchange();
        assertThat(result).hasStatusOk();

        verify(categoryService).delete(categoryId);
    }

    @Test
    void shouldDeleteEmployee() {
        UUID employeeId = UUID.randomUUID();
        doNothing().when(employeeService).deleteEmployee(employeeId);

        MvcTestResult result = mockMvc.delete().uri(BASE_URL + "/employees/{id}", employeeId)
                .with(csrf()).exchange();
        assertThat(result).hasStatusOk();

        verify(employeeService).deleteEmployee(employeeId);
    }

    @Test
    void shouldDeleteProduct() {
        UUID productId = UUID.randomUUID();
        doNothing().when(productService).delete(productId);

        MvcTestResult result = mockMvc.delete().uri(BASE_URL + "/products/{id}", productId)
                .with(csrf()).exchange();
        assertThat(result).hasStatusOk();

        verify(productService).delete(productId);
    }

    @Test
    void shouldGetAllEmployees() {
        UserResponseDTO first = sampleUser(UUID.randomUUID(), "employee.one");
        UserResponseDTO second = sampleUser(UUID.randomUUID(), "employee.two");
        when(employeeService.getEmployees()).thenReturn(List.of(first, second));

        MvcTestResult result = mockMvc.get().uri(BASE_URL + "/employees").exchange();
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$[1].id").asString()
                .isEqualTo(second.getId().toString());
        assertThat(result).bodyJson().extractingPath("$[1].username").asString()
                .isEqualTo("employee.two");

        verify(employeeService).getEmployees();
    }

    @Test
    void shouldGetEmployeeById() {
        UUID employeeId = UUID.randomUUID();
        UserResponseDTO employee = sampleUser(employeeId, "employee.by.id");
        when(employeeService.getEmployeeById(employeeId)).thenReturn(employee);

        MvcTestResult result = mockMvc.get().uri(BASE_URL + "/employees/{id}", employeeId).exchange();
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.id").asString()
                .isEqualTo(employeeId.toString());
        assertThat(result).bodyJson().extractingPath("$.username").asString()
                .isEqualTo("employee.by.id");

        verify(employeeService).getEmployeeById(employeeId);
    }

    @Test
    void shouldRegisterUser() {
        UserResponseDTO user = sampleUser(UUID.randomUUID(), "jane.smith");
        RegisterResponseDTO response = new RegisterResponseDTO(user, "User created successfully");
        when(authService.create(any(RegisterRequestDTO.class))).thenReturn(response);

        MvcTestResult result = mockMvc.post().uri(BASE_URL + "/employees").with(csrf())
                .contentType(APPLICATION_JSON).content(registerRequestJson()).exchange();
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.data.id").asString()
                .isEqualTo(user.getId().toString());
        assertThat(result).bodyJson().extractingPath("$.message").asString()
                .isEqualTo("User created successfully");

        verify(authService).create(any(RegisterRequestDTO.class));
    }

    @Test
    void shouldUpdateCategory() {
        UUID categoryId = UUID.randomUUID();
        CategoryResponseDTO response = sampleCategory(categoryId, "Updated Category");
        when(categoryService.update(eq(categoryId), any(CategoryUpdateRequestDTO.class)))
                .thenReturn(response);

        MvcTestResult result = mockMvc.put().uri(BASE_URL + "/categories/{id}", categoryId)
                .with(csrf()).contentType(APPLICATION_JSON)
                .content(categoryRequestJson("Updated Category")).exchange();
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.id").asString()
                .isEqualTo(categoryId.toString());
        assertThat(result).bodyJson().extractingPath("$.name").asString()
                .isEqualTo("Updated Category");

        verify(categoryService).update(eq(categoryId), any(CategoryUpdateRequestDTO.class));
    }

    @Test
    void shouldUpdateEmployee() {
        UUID employeeId = UUID.randomUUID();
        UserResponseDTO response = sampleUser(employeeId, "updated.user");

        when(employeeService.updateEmployee(eq(employeeId), any(UserUpdateRequestDTO.class)))
                .thenReturn(response);

        MvcTestResult result = mockMvc.put().uri(BASE_URL + "/employees/{id}", employeeId)
                .with(csrf()).contentType(APPLICATION_JSON).content("""
                        {
                            "firstName": "Updated",
                            "lastName": "User",
                            "username": "updated.user",
                            "password": "password123",
                            "email": "updated.user@example.com",
                            "role": "EMPLOYEE",
                            "phone": "+251933333333"
                        }
                            """).exchange();
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.id").asString()
                .isEqualTo(employeeId.toString());
        assertThat(result).bodyJson().extractingPath("$.username").asString()
                .isEqualTo("updated.user");

        verify(employeeService).updateEmployee(eq(employeeId), any(UserUpdateRequestDTO.class));
    }

    @Test
    void shouldUpdateProduct() {
        UUID productId = UUID.randomUUID();
        ProductResponseDTO response = sampleProduct(productId, "Coffee Premium");
        when(productService.update(eq(productId), any(ProductUpdateRequestDTO.class)))
                .thenReturn(response);

        MvcTestResult result = mockMvc.put().uri(BASE_URL + "/products/{id}", productId)
                .with(csrf()).contentType(APPLICATION_JSON).content(productUpdateRequestJson())
                .exchange();
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.id").asString()
                .isEqualTo(productId.toString());
        assertThat(result).bodyJson().extractingPath("$.name").asString()
                .isEqualTo("Coffee Premium");

        verify(productService).update(eq(productId), any(ProductUpdateRequestDTO.class));
    }
}
