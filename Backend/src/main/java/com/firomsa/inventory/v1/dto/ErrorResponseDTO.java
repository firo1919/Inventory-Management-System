package com.firomsa.inventory.v1.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponseDTO(
                int status,
                String message,
                LocalDateTime timestamp,
                Map<String, String> validationErrors) {
}
