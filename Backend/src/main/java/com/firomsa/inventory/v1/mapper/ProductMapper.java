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
    @Mapping(target = "createdAt", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "updatedAt", dateFormat = "yyyy-MM-dd HH:mm:ss")
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
