package com.firomsa.inventory.v1.dto;

import java.util.Map;

public record ErrorResponseDTO(int status, String message, String timestamp,
        Map<String, String> validationErrors) {
}
