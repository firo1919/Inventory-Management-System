package com.firomsa.inventory.v1.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ProductRequestDTO {

    @NotBlank
    private String name;

    @NotBlank
    private String sku;

    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal sellingPrice;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal costPrice;

    @NotNull
    @Min(0)
    private Integer quantity;

    @NotNull
    @Min(0)
    private Integer lowStockThreshold;

    private Set<UUID> categoryIds;
}
