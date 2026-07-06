package com.firomsa.inventory.v1.dto;

import java.math.BigDecimal;
import java.util.UUID;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleRequestDTO {
    @NotNull
    @Min(1)
    private Integer quantity;
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal salePrice;
    @NotNull
    private UUID productId;
}
