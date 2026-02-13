package com.firomsa.inventory.v1.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
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
public class ProductUpdateRequestDTO {

    private String name;

    private String sku;

    private String description;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal sellingPrice;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal costPrice;

    @Min(0)
    private Integer quantity;

    @Min(0)
    private Integer lowStockThreshold;

    private Boolean active;

    private Set<UUID> categoryIds;
}
