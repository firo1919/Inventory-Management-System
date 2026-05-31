package com.firomsa.inventory.v1.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.firomsa.inventory.v1.dto.RestockResponseDTO;
import com.firomsa.inventory.v1.dto.SaleResponseDTO;
import com.firomsa.inventory.v1.service.RestockService;
import com.firomsa.inventory.v1.service.SaleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/employee")
@Tag(name = "Employee", description = "API for employee operations")
@Slf4j
@RequiredArgsConstructor
public class EmployeeController {
    private final SaleService saleService;
    private final RestockService restockService;

    @Operation(summary = "For getting all sales recorded by the authenticated employee")
    @GetMapping("/sales")
    @ResponseStatus(HttpStatus.OK)
    public List<SaleResponseDTO> getMySales(Authentication authentication) {
        return saleService.getSalesByEmployee(authentication.getName());
    }

    @Operation(summary = "For getting all restocks recorded by the authenticated employee")
    @GetMapping("/restocks")
    @ResponseStatus(HttpStatus.OK)
    public List<RestockResponseDTO> getMyRestocks(Authentication authentication) {
        return restockService.getRestocksByEmployee(authentication.getName());
    }
}
