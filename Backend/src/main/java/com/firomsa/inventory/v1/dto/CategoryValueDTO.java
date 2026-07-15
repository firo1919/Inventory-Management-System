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
public class CategoryValueDTO implements Serializable {
    private UUID categoryId;
    private String categoryName;
    private BigDecimal value;
}
