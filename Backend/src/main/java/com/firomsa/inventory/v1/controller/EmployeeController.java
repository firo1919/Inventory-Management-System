package com.firomsa.inventory.v1.controller;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.firomsa.inventory.v1.dto.PageRequest;
import com.firomsa.inventory.v1.dto.PageResponse;
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
    public PageResponse<SaleResponseDTO> getMySales(Authentication authentication,
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
        return saleService.getSalesByEmployee(authentication.getName(), pageable);
    }

    @Operation(summary = "For getting all restocks recorded by the authenticated employee")
    @GetMapping("/restocks")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<RestockResponseDTO> getMyRestocks(Authentication authentication,
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
        return restockService.getRestocksByEmployee(authentication.getName(), pageable);
    }
}
