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

import com.firomsa.inventory.v1.dto.RestockRequestDTO;
import com.firomsa.inventory.v1.dto.RestockResponseDTO;
import com.firomsa.inventory.v1.service.RestockService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/restocks")
@Tag(name = "Restocks", description = "API for restock operations")
@Slf4j
public class RestockController {
    private final RestockService restockService;

    @Operation(summary = "For adding a restock record")
    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    public RestockResponseDTO createRestock(@Valid @RequestBody RestockRequestDTO restockRequestDTO,
            Authentication authentication) {
        return restockService.createRestock(restockRequestDTO, authentication.getName());
    }

    @Operation(summary = "For getting a restock record by id")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RestockResponseDTO getRestockById(@PathVariable UUID id) {
        return restockService.getRestockById(id);
    }
}
