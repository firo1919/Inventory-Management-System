package com.firomsa.inventory.v1.dto;

import java.math.BigDecimal;
import java.util.UUID;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockProductResponseDTO implements Serializable {
    private UUID id;
    private String name;
    private String sku;
    private Integer quantity;
    private Integer lowStockThreshold;
    private BigDecimal sellingPrice;
}
