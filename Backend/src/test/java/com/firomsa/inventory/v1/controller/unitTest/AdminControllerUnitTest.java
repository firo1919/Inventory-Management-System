package com.firomsa.inventory.v1.controller.unitTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
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
@AutoConfigureMockMvc(addFilters = false)
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
    private MockMvc mockMvc;

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
    void shouldReturnAllEmployees() throws Exception {
        UserResponseDTO user = sampleUser(UUID.randomUUID(), "employee.one");
        when(employeeService.getEmployees()).thenReturn(List.of(user));

        mockMvc.perform(get(BASE_URL + "/employees")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(user.getId().toString()))
                .andExpect(jsonPath("$[0].username").value("employee.one"));

        verify(employeeService).getEmployees();

    }

    @Test
    void shouldActivateProduct() throws Exception {
        UUID productId = UUID.randomUUID();
        doNothing().when(productService).activate(productId);

        mockMvc.perform(post(BASE_URL + "/products/{id}/activate", productId))
                .andExpect(status().isOk());

        verify(productService).activate(productId);

    }

    @Test
    void shouldAddProductImage() throws Exception {
        UUID productId = UUID.randomUUID();
        String objectKey = "products/image-1.jpg";
        ProductResponseDTO response = sampleProduct(productId, "Coffee");

        when(productService.addImageToProduct(productId, objectKey)).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/products/{id}/images", productId)
                .contentType(APPLICATION_JSON).content(fileDtoJson(objectKey)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.imageUrls[0]").value("https://cdn.example.com/image-1.jpg"));

        verify(productService).addImageToProduct(productId, objectKey);

    }

    @Test
    void shouldCreateCategory() throws Exception {
        CategoryResponseDTO response = sampleCategory(UUID.randomUUID(), "Beverages");
        when(categoryService.create(any(CategoryRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/categories").contentType(APPLICATION_JSON)
                .content(categoryRequestJson("Beverages"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId().toString()))
                .andExpect(jsonPath("$.name").value("Beverages"));

        verify(categoryService).create(any(CategoryRequestDTO.class));

    }

    @Test
    void shouldCreateProduct() throws Exception {
        ProductResponseDTO response = sampleProduct(UUID.randomUUID(), "Coffee");
        when(productService.create(any(ProductRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/products").contentType(APPLICATION_JSON)
                .content(productRequestJson())).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId().toString()))
                .andExpect(jsonPath("$.name").value("Coffee"));

        verify(productService).create(any(ProductRequestDTO.class));

    }

    @Test
    void shouldDeactivateEmployee() throws Exception {
        UUID employeeId = UUID.randomUUID();
        doNothing().when(employeeService).deactivateEmployee(employeeId);

        mockMvc.perform(post(BASE_URL + "/employees/{id}/deactivate", employeeId))
                .andExpect(status().isOk());

        verify(employeeService).deactivateEmployee(employeeId);

    }

    @Test
    void shouldDeactivateProduct() throws Exception {
        UUID productId = UUID.randomUUID();
        doNothing().when(productService).deactivate(productId);

        mockMvc.perform(post(BASE_URL + "/products/{id}/deactivate", productId))
                .andExpect(status().isOk());

        verify(productService).deactivate(productId);

    }

    @Test
    void shouldDeleteCategory() throws Exception {
        UUID categoryId = UUID.randomUUID();
        doNothing().when(categoryService).delete(categoryId);

        mockMvc.perform(delete(BASE_URL + "/categories/{id}", categoryId))
                .andExpect(status().isOk());

        verify(categoryService).delete(categoryId);

    }

    @Test
    void shouldDeleteEmployee() throws Exception {
        UUID employeeId = UUID.randomUUID();
        doNothing().when(employeeService).deleteEmployee(employeeId);

        mockMvc.perform(delete(BASE_URL + "/employees/{id}", employeeId))
                .andExpect(status().isOk());

        verify(employeeService).deleteEmployee(employeeId);

    }

    @Test
    void shouldDeleteProduct() throws Exception {
        UUID productId = UUID.randomUUID();
        doNothing().when(productService).delete(productId);

        mockMvc.perform(delete(BASE_URL + "/products/{id}", productId)).andExpect(status().isOk());

        verify(productService).delete(productId);

    }

    @Test
    void shouldGetAllEmployees() throws Exception {
        UserResponseDTO first = sampleUser(UUID.randomUUID(), "employee.one");
        UserResponseDTO second = sampleUser(UUID.randomUUID(), "employee.two");
        when(employeeService.getEmployees()).thenReturn(List.of(first, second));

        mockMvc.perform(get(BASE_URL + "/employees")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].id").value(second.getId().toString()))
                .andExpect(jsonPath("$[1].username").value("employee.two"));

        verify(employeeService).getEmployees();

    }

    @Test
    void shouldGetEmployeeById() throws Exception {
        UUID employeeId = UUID.randomUUID();
        UserResponseDTO employee = sampleUser(employeeId, "employee.by.id");
        when(employeeService.getEmployeeById(employeeId)).thenReturn(employee);

        mockMvc.perform(get(BASE_URL + "/employees/{id}", employeeId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employeeId.toString()))
                .andExpect(jsonPath("$.username").value("employee.by.id"));

        verify(employeeService).getEmployeeById(employeeId);

    }

    @Test
    void shouldRegisterUser() throws Exception {
        UserResponseDTO user = sampleUser(UUID.randomUUID(), "jane.smith");
        RegisterResponseDTO response = new RegisterResponseDTO(user, "User created successfully");
        when(authService.create(any(RegisterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/employees").contentType(APPLICATION_JSON)
                .content(registerRequestJson())).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.message").value("User created successfully"));

        verify(authService).create(any(RegisterRequestDTO.class));

    }

    @Test
    void shouldUpdateCategory() throws Exception {
        UUID categoryId = UUID.randomUUID();
        CategoryResponseDTO response = sampleCategory(categoryId, "Updated Category");
        when(categoryService.update(eq(categoryId), any(CategoryUpdateRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/categories/{id}", categoryId).contentType(APPLICATION_JSON)
                .content(categoryRequestJson("Updated Category"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Updated Category"));

        verify(categoryService).update(eq(categoryId), any(CategoryUpdateRequestDTO.class));

    }

    @Test
    void shouldUpdateEmployee() throws Exception {
        UUID employeeId = UUID.randomUUID();
        UserResponseDTO response = sampleUser(employeeId, "updated.user");

        when(employeeService.updateEmployee(eq(employeeId), any(UserUpdateRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/employees/{id}", employeeId).contentType(APPLICATION_JSON)
                .content("""
                        {
                            "firstName": "Updated",
                            "lastName": "User",
                            "username": "updated.user",
                            "password": "password123",
                            "email": "updated.user@example.com",
                            "role": "EMPLOYEE",
                            "phone": "+251933333333"
                        }
                        """)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employeeId.toString()))
                .andExpect(jsonPath("$.username").value("updated.user"));

        verify(employeeService).updateEmployee(eq(employeeId), any(UserUpdateRequestDTO.class));

    }

    @Test
    void shouldUpdateProduct() throws Exception {
        UUID productId = UUID.randomUUID();
        ProductResponseDTO response = sampleProduct(productId, "Coffee Premium");
        when(productService.update(eq(productId), any(ProductUpdateRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/products/{id}", productId).contentType(APPLICATION_JSON)
                .content(productUpdateRequestJson())).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Coffee Premium"));

        verify(productService).update(eq(productId), any(ProductUpdateRequestDTO.class));

    }
}
