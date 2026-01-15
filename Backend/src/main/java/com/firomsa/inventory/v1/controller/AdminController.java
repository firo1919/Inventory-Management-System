package com.firomsa.inventory.v1.controller;

import com.firomsa.inventory.v1.dto.RegisterRequestDTO;
import com.firomsa.inventory.v1.dto.RegisterResponseDTO;
import com.firomsa.inventory.v1.dto.UserResponseDTO;
import com.firomsa.inventory.v1.dto.UserUpdateRequestDTO;
import com.firomsa.inventory.v1.service.AdminService;
import com.firomsa.inventory.v1.service.AuthService;
import com.firomsa.inventory.v1.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
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

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Administrator", description = "API for admin operations")
@Slf4j
public class AdminController {

    private AuthService authService;
    private AdminService adminService;

    public AdminController(
        AuthService authService,
        AdminService adminService,
        UserService userService
    ) {
        this.authService = authService;
        this.adminService = adminService;
    }

    @Operation(summary = "For registering an employee")
    @PostMapping("/employees")
    @ResponseStatus(HttpStatus.OK)
    public RegisterResponseDTO registerUser(
        @Valid @RequestBody RegisterRequestDTO registerRequestDTO
    ) {
        var response = authService.create(registerRequestDTO);
        return response;
    }

    @Operation(summary = "For getting all employees")
    @GetMapping("/employees")
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponseDTO> getAllEmployees() {
        var response = adminService.getEmployees();
        return response;
    }

    @Operation(summary = "For getting an employee by id")
    @GetMapping("/employees/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDTO getEmployeeById(@PathVariable UUID id) {
        var response = adminService.getEmployeeById(id);
        return response;
    }

    @Operation(summary = "For updating an employee by id")
    @PutMapping("/employees/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDTO updateEmployee(
        @PathVariable UUID id,
        @Valid @RequestBody UserUpdateRequestDTO userUpdateRequestDTO
    ) {
        var response = adminService.updateEmployee(id, userUpdateRequestDTO);
        return response;
    }

    @Operation(summary = "For deactivating an employee by id")
    @PostMapping("/employees/{id}/deactivate")
    @ResponseStatus(HttpStatus.OK)
    public void deactivateEmployee(@PathVariable UUID id) {
        adminService.deactivateEmployee(id);
    }

    @Operation(summary = "For activating an employee by id")
    @PostMapping("/employees/{id}/activate")
    @ResponseStatus(HttpStatus.OK)
    public void activateEmployee(@PathVariable UUID id) {
        adminService.activateEmployee(id);
    }

    @Operation(summary = "For deleting an employee by id")
    @DeleteMapping("/employees/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteEmployee(@PathVariable UUID id) {
        adminService.deleteEmployee(id);
    }
}
