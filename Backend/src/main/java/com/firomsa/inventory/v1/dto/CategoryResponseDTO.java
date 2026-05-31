package com.firomsa.inventory.v1.dto;

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

    private String createdAt;

    private String updatedAt;

    private Set<UUID> productIds;
}
