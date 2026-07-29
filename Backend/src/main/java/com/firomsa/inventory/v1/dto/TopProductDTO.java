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
public class TopProductDTO {
    private UUID productId;
    private String productName;
    private long totalQuantitySold;
    private BigDecimal totalRevenue;
    private long totalTimesRestocked;
}
