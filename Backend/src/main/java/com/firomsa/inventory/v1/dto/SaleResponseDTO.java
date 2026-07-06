package com.firomsa.inventory.v1.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponseDTO {
    private UUID id;
    private Integer quantity;
    private BigDecimal salePrice;
    private UUID productId;
    private String timestamp;
    private String message;
}
