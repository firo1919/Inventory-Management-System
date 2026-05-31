package com.firomsa.inventory.v1.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.firomsa.inventory.v1.dto.SaleRequestDTO;
import com.firomsa.inventory.v1.dto.SaleResponseDTO;
import com.firomsa.inventory.v1.service.SaleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sales")
@Tag(name = "Sales", description = "API for sales operations")
@Slf4j
public class SaleController {
    private final SaleService saleService;

    @Operation(summary = "For adding a sales record")
    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    public SaleResponseDTO createSale(@Valid @RequestBody SaleRequestDTO saleRequestDTO,
            Authentication authentication) {
        return saleService.createSale(saleRequestDTO, authentication.getName());
    }

    @Operation(summary = "For getting a sale record by id")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SaleResponseDTO getSaleById(@PathVariable UUID id) {
        return saleService.getSaleById(id);
    }

}
