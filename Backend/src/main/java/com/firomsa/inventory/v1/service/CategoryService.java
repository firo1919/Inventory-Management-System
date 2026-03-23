package com.firomsa.inventory.v1.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.firomsa.inventory.exception.ResourceNotFoundException;
import com.firomsa.inventory.model.Category;
import com.firomsa.inventory.repository.CategoryRepository;
import com.firomsa.inventory.v1.dto.CategoryRequestDTO;
import com.firomsa.inventory.v1.dto.CategoryResponseDTO;
import com.firomsa.inventory.v1.dto.CategoryUpdateRequestDTO;
import com.firomsa.inventory.v1.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAll() {
        return categoryRepository.findAll().stream().map(categoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO getById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        return categoryMapper.toDTO(category);
    }

    public CategoryResponseDTO create(CategoryRequestDTO request) {
        Category category = categoryMapper.toModel(request);
        Category saved = categoryRepository.save(category);
        return categoryMapper.toDTO(saved);
    }

    public CategoryResponseDTO update(UUID id,
            CategoryUpdateRequestDTO request) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

        categoryMapper.updateModelFromDTO(request, existing);

        Category saved = categoryRepository.save(existing);
        return categoryMapper.toDTO(saved);
    }

    public void delete(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
