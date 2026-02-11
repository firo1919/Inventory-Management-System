package com.firomsa.inventory.v1.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDTO {

    private UUID id;

    private String name;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Set<UUID> productIds;
}
