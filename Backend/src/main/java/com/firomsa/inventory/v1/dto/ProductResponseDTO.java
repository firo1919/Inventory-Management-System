package com.firomsa.inventory.v1.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
public class ProductResponseDTO {

    private UUID id;

    private String name;

    private String sku;

    private String description;

    private BigDecimal sellingPrice;

    private BigDecimal costPrice;

    private Integer quantity;

    private Integer lowStockThreshold;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Set<UUID> categoryIds;

    private List<String> imageUrls;
}
