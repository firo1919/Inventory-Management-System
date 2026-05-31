package com.firomsa.inventory.v1.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.firomsa.inventory.exception.ResourceNotFoundException;
import com.firomsa.inventory.model.Category;
import com.firomsa.inventory.repository.CategoryRepository;
import com.firomsa.inventory.v1.dto.CategoryRequestDTO;
import com.firomsa.inventory.v1.dto.CategoryResponseDTO;
import com.firomsa.inventory.v1.dto.CategoryUpdateRequestDTO;
import com.firomsa.inventory.v1.mapper.CategoryMapper;

@ExtendWith(MockitoExtension.class)
class CategoryServiceUnitTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;
    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryResponseDTO categoryResponseDTO;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        category = new Category();
        category.setId(categoryId);
        category.setName("Electronics");

        categoryResponseDTO = new CategoryResponseDTO();
        categoryResponseDTO.setId(categoryId);
        categoryResponseDTO.setName("Electronics");
    }

    @Test
    @DisplayName("Should return all categories")
    void getAll_ShouldReturnListOfCategories() {
        // Arrange
        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(categoryMapper.toDTO(category)).thenReturn(categoryResponseDTO);

        // Act
        List<CategoryResponseDTO> result = categoryService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(categoryResponseDTO.getName(), result.get(0).getName());
        verify(categoryRepository, times(1)).findAll();
        verify(categoryMapper, times(1)).toDTO(category);
    }

    @Test
    @DisplayName("Should return category by ID when exists")
    void getById_WhenExists_ShouldReturnCategory() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryMapper.toDTO(category)).thenReturn(categoryResponseDTO);

        // Act
        CategoryResponseDTO result = categoryService.getById(categoryId);

        // Assert
        assertNotNull(result);
        assertEquals(categoryId, result.getId());
        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when category ID does not exist")
    void getById_WhenDoesNotExist_ShouldThrowException() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> categoryService.getById(categoryId));
        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    @DisplayName("Should create and return new category")
    void create_ShouldSaveAndReturnCategory() {
        // Arrange
        CategoryRequestDTO requestDTO = new CategoryRequestDTO("Electronics");
        when(categoryMapper.toModel(requestDTO)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDTO(category)).thenReturn(categoryResponseDTO);

        // Act
        CategoryResponseDTO result = categoryService.create(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Electronics", result.getName());
        verify(categoryMapper, times(1)).toModel(requestDTO);
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    @DisplayName("Should update existing category")
    void update_WhenExists_ShouldUpdateAndReturnCategory() {
        // Arrange
        CategoryUpdateRequestDTO updateRequest = new CategoryUpdateRequestDTO("Updated Name");
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        doNothing().when(categoryMapper).updateModelFromDTO(updateRequest, category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDTO(category)).thenReturn(categoryResponseDTO);

        // Act
        CategoryResponseDTO result = categoryService.update(categoryId, updateRequest);

        // Assert
        assertNotNull(result);
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryMapper, times(1)).updateModelFromDTO(updateRequest, category);
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent category")
    void update_WhenDoesNotExist_ShouldThrowException() {
        // Arrange
        CategoryUpdateRequestDTO updateRequest = new CategoryUpdateRequestDTO("Updated Name");
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.update(categoryId, updateRequest));
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete category when exists")
    void delete_WhenExists_ShouldDeleteCategory() {
        // Arrange
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        doNothing().when(categoryRepository).deleteById(categoryId);

        // Act
        categoryService.delete(categoryId);

        // Assert
        verify(categoryRepository, times(1)).existsById(categoryId);
        verify(categoryRepository, times(1)).deleteById(categoryId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent category")
    void delete_WhenDoesNotExist_ShouldThrowException() {
        // Arrange
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> categoryService.delete(categoryId));
        verify(categoryRepository, times(1)).existsById(categoryId);
        verify(categoryRepository, never()).deleteById(any());
    }
}
