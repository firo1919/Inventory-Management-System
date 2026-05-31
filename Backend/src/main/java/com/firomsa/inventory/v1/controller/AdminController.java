package com.firomsa.inventory.v1.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.firomsa.inventory.v1.dto.CategoryRequestDTO;
import com.firomsa.inventory.v1.dto.CategoryResponseDTO;
import com.firomsa.inventory.v1.dto.CategoryUpdateRequestDTO;
import com.firomsa.inventory.v1.dto.FileDTO;
import com.firomsa.inventory.v1.dto.ProductRequestDTO;
import com.firomsa.inventory.v1.dto.ProductResponseDTO;
import com.firomsa.inventory.v1.dto.ProductUpdateRequestDTO;
import com.firomsa.inventory.v1.dto.RegisterRequestDTO;
import com.firomsa.inventory.v1.dto.RegisterResponseDTO;
import com.firomsa.inventory.v1.dto.RestockResponseDTO;
import com.firomsa.inventory.v1.dto.SaleResponseDTO;
import com.firomsa.inventory.v1.dto.UserResponseDTO;
import com.firomsa.inventory.v1.dto.UserUpdateRequestDTO;
import com.firomsa.inventory.v1.service.AuthService;
import com.firomsa.inventory.v1.service.CategoryService;
import com.firomsa.inventory.v1.service.EmployeeService;
import com.firomsa.inventory.v1.service.ProductService;
import com.firomsa.inventory.v1.service.RestockService;
import com.firomsa.inventory.v1.service.SaleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Administrator", description = "API for admin operations")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

    private final AuthService authService;
    private final EmployeeService employeeService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final SaleService saleService;
    private final RestockService restockService;

    // Employee management endpoints
    @Operation(summary = "For registering an employee")
    @PostMapping("/employees")
    @ResponseStatus(HttpStatus.OK)
    public RegisterResponseDTO registerUser(
            @Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        var response = authService.create(registerRequestDTO);
        return response;
    }

    @Operation(summary = "For getting all employees")
    @GetMapping("/employees")
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponseDTO> getAllEmployees() {
        var response = employeeService.getEmployees();
        return response;
    }

    @Operation(summary = "For getting an employee by id")
    @GetMapping("/employees/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDTO getEmployeeById(@PathVariable UUID id) {
        var response = employeeService.getEmployeeById(id);
        return response;
    }

    @Operation(summary = "For updating an employee by id")
    @PutMapping("/employees/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDTO updateEmployee(@PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequestDTO userUpdateRequestDTO) {
        var response = employeeService.updateEmployee(id, userUpdateRequestDTO);
        return response;
    }

    @Operation(summary = "For deactivating an employee by id")
    @PostMapping("/employees/{id}/deactivate")
    @ResponseStatus(HttpStatus.OK)
    public void deactivateEmployee(@PathVariable UUID id) {
        employeeService.deactivateEmployee(id);
    }

    @Operation(summary = "For activating an employee by id")
    @PostMapping("/employees/{id}/activate")
    @ResponseStatus(HttpStatus.OK)
    public void activateEmployee(@PathVariable UUID id) {
        employeeService.activateEmployee(id);
    }

    @Operation(summary = "For deleting an employee by id")
    @DeleteMapping("/employees/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteEmployee(@PathVariable UUID id) {
        employeeService.deleteEmployee(id);
    }

    // Product management endpoints
    @Operation(summary = "Create a product")
    @PostMapping("/products")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponseDTO createProduct(
            @Valid @RequestBody ProductRequestDTO productRequestDTO) {
        var response = productService.create(productRequestDTO);
        return response;
    }

    @Operation(summary = "Update a product by id")
    @PutMapping("/products/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponseDTO updateProduct(@PathVariable UUID id,
            @Valid @RequestBody ProductUpdateRequestDTO productUpdateRequestDTO) {
        var response = productService.update(id, productUpdateRequestDTO);
        return response;
    }

    // Product management endpoints
    @Operation(summary = "Delete a product by id")
    @DeleteMapping("/products/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteProduct(@PathVariable UUID id) {
        productService.delete(id);
    }

    @Operation(summary = "Activate a product by id")
    @PostMapping("/products/{id}/activate")
    @ResponseStatus(HttpStatus.OK)
    public void activateProduct(@PathVariable UUID id) {
        productService.activate(id);
    }

    @Operation(summary = "Deactivate a product by id")
    @PostMapping("/products/{id}/deactivate")
    @ResponseStatus(HttpStatus.OK)
    public void deactivateProduct(@PathVariable UUID id) {
        productService.deactivate(id);
    }

    @Operation(summary = "Add an image to a product by id")
    @PostMapping("/products/{id}/images")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponseDTO addProductImage(@PathVariable UUID id,
            @Valid @RequestBody FileDTO productImageDTO) {
        var response = productService.addImageToProduct(id, productImageDTO.getObjectKey());
        return response;
    }

    // Category management endpoints
    @Operation(summary = "Create a category")
    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.OK)
    public CategoryResponseDTO createCategory(
            @Valid @RequestBody CategoryRequestDTO categoryRequestDTO) {
        var response = categoryService.create(categoryRequestDTO);
        return response;
    }

    @Operation(summary = "Update a category by id")
    @PutMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryResponseDTO updateCategory(@PathVariable UUID id,
            @Valid @RequestBody CategoryUpdateRequestDTO categoryUpdateRequestDTO) {
        var response = categoryService.update(id, categoryUpdateRequestDTO);
        return response;
    }

    @Operation(summary = "Delete a category by id")
    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteCategory(@PathVariable UUID id) {
        categoryService.delete(id);
    }

    @Operation(summary = "For getting all sales")
    @GetMapping("/sales")
    @ResponseStatus(HttpStatus.OK)
    public List<SaleResponseDTO> getAllSales() {
        return saleService.getAllSales();
    }

    @Operation(summary = "For deleting a sale record by id")
    @DeleteMapping("/sales/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteSaleById(@PathVariable UUID id) {
        saleService.deleteSaleById(id);
    }

    @Operation(summary = "For getting all restocks")
    @GetMapping("/restocks")
    @ResponseStatus(HttpStatus.OK)
    public List<RestockResponseDTO> getAllRestocks() {
        return restockService.getAllRestocks();
    }

    @Operation(summary = "For deleting a restock record by id")
    @DeleteMapping("/restocks/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteRestockById(@PathVariable UUID id) {
        restockService.deleteRestockById(id);
    }
}
