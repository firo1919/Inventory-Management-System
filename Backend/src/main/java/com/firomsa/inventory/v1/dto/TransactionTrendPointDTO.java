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
public class TransactionTrendPointDTO {
    private String period;
    private long salesCount;
    private BigDecimal salesRevenue;
    private long restocksCount;
    private long unitsSold;
    private long unitsRestocked;
}
