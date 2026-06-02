package com.firomsa.inventory.v1.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryValueResponseDTO {
    private BigDecimal totalValue;
    private List<CategoryValueDTO> categories;
}
