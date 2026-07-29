package com.firomsa.inventory.v1.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummaryDTO {
    private long totalSalesCount;
    private BigDecimal totalSalesRevenue;
    private long totalUnitsSold;
    private long totalRestocksCount;
    private long totalUnitsRestocked;
    private String startDate;
    private String endDate;
}
