package com.firomsa.inventory.v1.dto;

import java.util.UUID;

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
public class RestockRequestDTO {
    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    private UUID productId;
}
