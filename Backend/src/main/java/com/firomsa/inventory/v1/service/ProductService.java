package com.firomsa.inventory.v1.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.firomsa.inventory.exception.ResourceNotFoundException;
import com.firomsa.inventory.model.Category;
import com.firomsa.inventory.model.Product;
import com.firomsa.inventory.repository.CategoryRepository;
import com.firomsa.inventory.repository.ProductRepository;
import com.firomsa.inventory.v1.dto.CategoryValueDTO;
import com.firomsa.inventory.v1.dto.InventoryValueResponseDTO;
import com.firomsa.inventory.v1.dto.LowStockProductResponseDTO;
import com.firomsa.inventory.v1.dto.PageResponse;
import com.firomsa.inventory.v1.dto.ProductRequestDTO;
import com.firomsa.inventory.v1.dto.ProductResponseDTO;
import com.firomsa.inventory.v1.dto.ProductUpdateRequestDTO;
import com.firomsa.inventory.v1.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAll() {
        var response = productRepository.findAll().stream().map(productMapper::toDTO)
                .collect(Collectors.toList());

        // Fetch image URLs for each product
        response.forEach(productResponse -> {
            var imageUrls = productRepository.findById(productResponse.getId())
                    .map(Product::getImageKeys).orElse(List.of()).stream()
                    .map(storageService::getUrl).collect(Collectors.toList());
            productResponse.setImageUrls(imageUrls);
        });
        return response;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponseDTO> getAll(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);
        var response = productPage.getContent().stream().map(productMapper::toDTO)
                .collect(Collectors.toList());

        // Fetch image URLs for each product
        response.forEach(productResponse -> {
            var imageUrls = productRepository.findById(productResponse.getId())
                    .map(Product::getImageKeys).orElse(List.of()).stream()
                    .map(storageService::getUrl).collect(Collectors.toList());
            productResponse.setImageUrls(imageUrls);
        });

        return PageResponse.<ProductResponseDTO>builder()
                .content(response)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .empty(productPage.isEmpty())
                .build();
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        var response = productMapper.toDTO(product);
        var imageUrls = product.getImageKeys().stream().map(storageService::getUrl)
                .collect(Collectors.toList());
        response.setImageUrls(imageUrls);
        return response;
    }

    public ProductResponseDTO create(ProductRequestDTO request) {
        Product product = productMapper.toModel(request);
        if (request.getCategoryIds() != null) {
            Set<Category> categories = categoryRepository.findAllById(request.getCategoryIds())
                    .stream().collect(Collectors.toSet());
            product.setCategories(categories);
        }
        Product saved = productRepository.save(product);
        var response = productMapper.toDTO(saved);
        var imageUrls = saved.getImageKeys().stream().map(storageService::getUrl)
                .collect(Collectors.toList());
        response.setImageUrls(imageUrls);
        return response;
    }

    public ProductResponseDTO update(UUID id,
            ProductUpdateRequestDTO request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        // Update mutable fields from DTO
        productMapper.updateModelFromDTO(request, existing);
        if (request.getCategoryIds() != null) {
            Set<Category> categories = categoryRepository.findAllById(request.getCategoryIds())
                    .stream().collect(Collectors.toSet());
            existing.setCategories(categories);
        }
        Product saved = productRepository.save(existing);
        var response = productMapper.toDTO(saved);
        var imageUrls = saved.getImageKeys().stream().map(storageService::getUrl)
                .collect(Collectors.toList());
        response.setImageUrls(imageUrls);
        return response;
    }

    public void delete(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    public void activate(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        product.setActive(true);
        productRepository.save(product);
    }

    public void deactivate(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        product.setActive(false);
        productRepository.save(product);
    }

    public ProductResponseDTO addImageToProduct(UUID id, String objectKey) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        if (storageService.exists(objectKey)) {
            product.getImageKeys().add(objectKey);
            Product saved = productRepository.save(product);
            var response = productMapper.toDTO(saved);
            var imageUrls = saved.getImageKeys().stream().map(storageService::getUrl)
                    .collect(Collectors.toList());
            response.setImageUrls(imageUrls);
            return response;
        } else {
            throw new ResourceNotFoundException("Image not found in storage: " + objectKey);
        }

    }

    @Transactional(readOnly = true)
    public List<LowStockProductResponseDTO> getLowStockProducts() {
        return productRepository.findByQuantityLessThanLowStockThreshold().stream()
                .map(product -> LowStockProductResponseDTO.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .sku(product.getSku())
                        .quantity(product.getQuantity())
                        .lowStockThreshold(product.getLowStockThreshold())
                        .sellingPrice(product.getSellingPrice())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<LowStockProductResponseDTO> getLowStockProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findByQuantityLessThanLowStockThreshold(pageable);
        var response = productPage.getContent().stream()
                .map(product -> LowStockProductResponseDTO.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .sku(product.getSku())
                        .quantity(product.getQuantity())
                        .lowStockThreshold(product.getLowStockThreshold())
                        .sellingPrice(product.getSellingPrice())
                        .build())
                .collect(Collectors.toList());

        return PageResponse.<LowStockProductResponseDTO>builder()
                .content(response)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .empty(productPage.isEmpty())
                .build();
    }

    @Transactional(readOnly = true)
    public InventoryValueResponseDTO getInventoryValueReport() {
        BigDecimal totalValue = productRepository.calculateTotalInventoryValue();

        List<CategoryValueDTO> categoryValues = categoryRepository.findAll().stream()
                .map(category -> CategoryValueDTO.builder()
                        .categoryId(category.getId())
                        .categoryName(category.getName())
                        .value(productRepository.calculateInventoryValueByCategory(category.getId()))
                        .build())
                .filter(dto -> dto.getValue().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        return InventoryValueResponseDTO.builder()
                .totalValue(totalValue)
                .categories(categoryValues)
                .build();
    }
}
