package com.firomsa.inventory.v1.mapper;

import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.firomsa.inventory.model.Product;
import com.firomsa.inventory.model.Sale;
import com.firomsa.inventory.v1.dto.SaleRequestDTO;
import com.firomsa.inventory.v1.dto.SaleResponseDTO;

@Mapper(componentModel = "spring")
public interface SaleMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "soldBy", ignore = true)
    Sale toModel(SaleRequestDTO saleRequestDTO);

    @Mapping(target = "message", ignore = true)
    @Mapping(target = "timestamp", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "productId", expression = "java(mapProductToUUID(sale.getProduct()))")
    SaleResponseDTO toDTO(Sale sale);

    default UUID mapProductToUUID(Product product) {
        if (product == null) {
            return null;
        }
        return product.getId();
    }
}
