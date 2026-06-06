package com.firomsa.inventory.v1.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.firomsa.inventory.config.CacheConfig;
import com.firomsa.inventory.exception.ResourceNotFoundException;
import com.firomsa.inventory.model.Category;
import com.firomsa.inventory.repository.CategoryRepository;
import com.firomsa.inventory.v1.dto.CategoryRequestDTO;
import com.firomsa.inventory.v1.dto.CategoryResponseDTO;
import com.firomsa.inventory.v1.dto.CategoryUpdateRequestDTO;
import com.firomsa.inventory.v1.dto.PageResponse;
import com.firomsa.inventory.v1.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CATEGORIES, key = "'all'")
    public List<CategoryResponseDTO> getAll() {
        return categoryRepository.findAll().stream().map(categoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CATEGORIES,
            key = "'page:' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
    public PageResponse<CategoryResponseDTO> getAll(Pageable pageable) {
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        var response = categoryPage.getContent().stream().map(categoryMapper::toDTO)
                .collect(Collectors.toList());

        return PageResponse.<CategoryResponseDTO>builder()
                .content(response)
                .pageNumber(categoryPage.getNumber())
                .pageSize(categoryPage.getSize())
                .totalElements(categoryPage.getTotalElements())
                .totalPages(categoryPage.getTotalPages())
                .first(categoryPage.isFirst())
                .last(categoryPage.isLast())
                .empty(categoryPage.isEmpty())
                .build();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CATEGORIES, key = "'id:' + #id")
    public CategoryResponseDTO getById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        return categoryMapper.toDTO(category);
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CATEGORIES, allEntries = true),
            @CacheEvict(value = CacheConfig.PRODUCTS, allEntries = true),
            @CacheEvict(value = CacheConfig.INVENTORY_VALUE, allEntries = true)})
    public CategoryResponseDTO create(CategoryRequestDTO request) {
        Category category = categoryMapper.toModel(request);
        Category saved = categoryRepository.save(category);
        return categoryMapper.toDTO(saved);
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CATEGORIES, allEntries = true),
            @CacheEvict(value = CacheConfig.PRODUCTS, allEntries = true),
            @CacheEvict(value = CacheConfig.INVENTORY_VALUE, allEntries = true)})
    public CategoryResponseDTO update(UUID id,
            CategoryUpdateRequestDTO request) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

        categoryMapper.updateModelFromDTO(request, existing);

        Category saved = categoryRepository.save(existing);
        return categoryMapper.toDTO(saved);
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CATEGORIES, allEntries = true),
            @CacheEvict(value = CacheConfig.PRODUCTS, allEntries = true),
            @CacheEvict(value = CacheConfig.INVENTORY_VALUE, allEntries = true)})
    public void delete(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
