package com.firomsa.inventory.v1.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.firomsa.inventory.model.Product;
import com.firomsa.inventory.model.Restock;
import com.firomsa.inventory.v1.dto.RestockRequestDTO;
import com.firomsa.inventory.v1.dto.RestockResponseDTO;

@Mapper(componentModel = "spring")
public interface RestockMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "restockedBy", ignore = true)
    Restock toModel(RestockRequestDTO restockRequestDTO);

    @Mapping(target = "message", ignore = true)
    @Mapping(target = "timestamp", dateFormat = "dd.MM.yyyy")
    @Mapping(target = "productId", expression = "java(mapProductToUUID(restock.getProduct()))")
    RestockResponseDTO toDTO(Restock restock);

    default UUID mapProductToUUID(Product product) {
        if (product == null) {
            return null;
        }
        return product.getId();
    }
}
