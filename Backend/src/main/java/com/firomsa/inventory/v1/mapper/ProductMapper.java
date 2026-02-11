package com.firomsa.inventory.v1.mapper;

import com.firomsa.inventory.model.Category;
import com.firomsa.inventory.model.Product;
import com.firomsa.inventory.v1.dto.ProductRequestDTO;
import com.firomsa.inventory.v1.dto.ProductResponseDTO;
import com.firomsa.inventory.v1.dto.ProductUpdateRequestDTO;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    // Entity -> Response DTO
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "sku", source = "sku")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "sellingPrice", source = "sellingPrice")
    @Mapping(target = "costPrice", source = "costPrice")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "lowStockThreshold", source = "lowStockThreshold")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "imageUrls", ignore = true)
    @Mapping(target = "categoryIds", expression = "java(toCategoryIds(product.getCategories()))")
    ProductResponseDTO toDTO(Product product);

    // Request DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "imageKeys", ignore = true)
    Product toModel(ProductRequestDTO request);

    // Partial update: only set non-null fields from update DTO
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "imageKeys", ignore = true)
    void updateModelFromDTO(ProductUpdateRequestDTO update, @MappingTarget Product product);

    // Helper to convert categories to UUIDs for the response
    default Set<UUID> toCategoryIds(Set<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return Set.of();
        }
        return categories.stream().map(Category::getId).collect(Collectors.toSet());
    }
    
}
