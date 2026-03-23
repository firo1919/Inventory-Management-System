package com.firomsa.inventory.v1.mapper;

import com.firomsa.inventory.model.Product;
import com.firomsa.inventory.model.Category;
import com.firomsa.inventory.v1.dto.CategoryRequestDTO;
import com.firomsa.inventory.v1.dto.CategoryResponseDTO;
import com.firomsa.inventory.v1.dto.CategoryUpdateRequestDTO;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    // Entity -> Response DTO
    @Mapping(target = "createdAt", dateFormat = "dd.MM.yyyy")
    @Mapping(target = "updatedAt", dateFormat = "dd.MM.yyyy")
    @Mapping(target = "productIds", expression = "java(toProductIds(category.getProducts()))")
    CategoryResponseDTO toDTO(Category category);

    // Request DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toModel(CategoryRequestDTO request);

    // Partial update: ignore nulls from update DTO
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateModelFromDTO(CategoryUpdateRequestDTO update, @MappingTarget Category category);

    // Helper: map associated products to their IDs for response DTO
    default Set<UUID> toProductIds(Set<Product> products) {
        if (products == null || products.isEmpty()) {
            return Set.of();
        }
        return products.stream().map(Product::getId).collect(Collectors.toSet());
    }
}
