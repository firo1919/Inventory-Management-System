package com.firomsa.inventory.v1.controller;

import java.util.UUID;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.firomsa.inventory.v1.dto.LowStockProductResponseDTO;
import com.firomsa.inventory.v1.dto.PageRequest;
import com.firomsa.inventory.v1.dto.PageResponse;
import com.firomsa.inventory.v1.dto.ProductResponseDTO;
import com.firomsa.inventory.v1.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Product", description = "API for product operations")
@Slf4j
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Get all products")
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<ProductResponseDTO> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        var pageable = PageRequest.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection != null
                        ? sortDirection.equalsIgnoreCase("desc") ? Direction.DESC
                                : Direction.ASC
                        : Direction.ASC)
                .build()
                .toPageable();
        return productService.getAll(pageable);
    }

    @Operation(summary = "Get a product by id")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponseDTO getProductById(@PathVariable UUID id) {
        var response = productService.getById(id);
        return response;
    }

    @Operation(summary = "Get all low stock products")
    @GetMapping("/low-stock")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<LowStockProductResponseDTO> getLowStockProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        var pageable = PageRequest.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection != null
                        ? sortDirection.equalsIgnoreCase("desc") ? Direction.DESC
                                : Direction.ASC
                        : Direction.ASC)
                .build()
                .toPageable();
        return productService.getLowStockProducts(pageable);
    }
}
