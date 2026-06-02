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
public class CategoryValueDTO {
    private UUID categoryId;
    private String categoryName;
    private BigDecimal value;
}
