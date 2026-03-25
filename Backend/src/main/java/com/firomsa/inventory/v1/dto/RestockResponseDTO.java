package com.firomsa.inventory.v1.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestockResponseDTO {
    private Integer quantity;
    private UUID productId;
    private LocalDateTime timestamp;
    private String message;
}
