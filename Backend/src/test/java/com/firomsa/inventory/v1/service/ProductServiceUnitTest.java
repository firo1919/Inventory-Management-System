package com.firomsa.inventory.v1.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import com.firomsa.inventory.model.Product;
import com.firomsa.inventory.repository.CategoryRepository;
import com.firomsa.inventory.repository.ProductRepository;
import com.firomsa.inventory.v1.dto.ProductRequestDTO;
import com.firomsa.inventory.v1.dto.ProductResponseDTO;
import com.firomsa.inventory.v1.dto.ProductUpdateRequestDTO;
import com.firomsa.inventory.v1.mapper.ProductMapper;

@ExtendWith(MockitoExtension.class)
class ProductServiceUnitTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private StorageService storageService;
    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductResponseDTO productResponseDTO;
    private UUID productId;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        product = new Product();
        product.setId(productId);
        product.setName("Laptop");
        product.setDescription("Gaming Laptop");
        product.setSellingPrice(java.math.BigDecimal.valueOf(1500.0));
        product.setQuantity(10);
        product.setImageKeys(new ArrayList<>(List.of("image1.jpg")));

        productResponseDTO = new ProductResponseDTO();
        productResponseDTO.setId(productId);
        productResponseDTO.setName("Laptop");
        productResponseDTO.setDescription("Gaming Laptop");
        productResponseDTO.setSellingPrice(java.math.BigDecimal.valueOf(1500.0));
        productResponseDTO.setQuantity(10);
    }

    @Test
    @DisplayName("Should return all products with image URLs")
    void getAll_ShouldReturnListOfProducts() {
        // Arrange
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(productMapper.toDTO(product)).thenReturn(productResponseDTO);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(storageService.getUrl("image1.jpg")).thenReturn("http://example.com/image1.jpg");

        // Act
        List<ProductResponseDTO> result = productService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getName());
        assertEquals(1, result.get(0).getImageUrls().size());
        assertEquals("http://example.com/image1.jpg", result.get(0).getImageUrls().get(0));

        verify(productRepository, times(1)).findAll();
        verify(productMapper, times(1)).toDTO(product);
    }

    @Test
    @DisplayName("Should return product by ID with image URLs")
    void getById_WhenExists_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toDTO(product)).thenReturn(productResponseDTO);
        when(storageService.getUrl("image1.jpg")).thenReturn("http://example.com/image1.jpg");

        // Act
        ProductResponseDTO result = productService.getById(productId);

        // Assert
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals(1, result.getImageUrls().size());
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product ID does not exist")
    void getById_WhenDoesNotExist_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.getById(productId));
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Should create and return new product")
    void create_ShouldSaveAndReturnProduct() {
        // Arrange
        ProductRequestDTO requestDTO = new ProductRequestDTO();
        requestDTO.setName("Laptop");
        requestDTO.setCategoryIds(Set.of(categoryId));

        Category category = new Category();
        category.setId(categoryId);

        when(productMapper.toModel(requestDTO)).thenReturn(product);
        when(categoryRepository.findAllById(Set.of(categoryId))).thenReturn(List.of(category));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDTO(product)).thenReturn(productResponseDTO);
        when(storageService.getUrl("image1.jpg")).thenReturn("http://example.com/image1.jpg");

        // Act
        ProductResponseDTO result = productService.create(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Laptop", result.getName());
        verify(categoryRepository, times(1)).findAllById(Set.of(categoryId));
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("Should update existing product")
    void update_WhenExists_ShouldUpdateAndReturnProduct() {
        // Arrange
        ProductUpdateRequestDTO updateRequest = new ProductUpdateRequestDTO();
        updateRequest.setName("Updated Laptop");
        updateRequest.setCategoryIds(Set.of(categoryId));

        Category category = new Category();
        category.setId(categoryId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        doNothing().when(productMapper).updateModelFromDTO(updateRequest, product);
        when(categoryRepository.findAllById(Set.of(categoryId))).thenReturn(List.of(category));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDTO(product)).thenReturn(productResponseDTO);
        when(storageService.getUrl("image1.jpg")).thenReturn("http://example.com/image1.jpg");

        // Act
        ProductResponseDTO result = productService.update(productId, updateRequest);

        // Assert
        assertNotNull(result);
        verify(productRepository, times(1)).findById(productId);
        verify(productMapper, times(1)).updateModelFromDTO(updateRequest, product);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("Should delete product when exists")
    void delete_WhenExists_ShouldDeleteProduct() {
        // Arrange
        when(productRepository.existsById(productId)).thenReturn(true);
        doNothing().when(productRepository).deleteById(productId);

        // Act
        productService.delete(productId);

        // Assert
        verify(productRepository, times(1)).existsById(productId);
        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent product")
    void delete_WhenDoesNotExist_ShouldThrowException() {
        // Arrange
        when(productRepository.existsById(productId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.delete(productId));
        verify(productRepository, times(1)).existsById(productId);
        verify(productRepository, never()).deleteById(productId);
    }

    @Test
    @DisplayName("Should activate product when exists")
    void activate_WhenExists_ShouldSetActiveTrue() {
        // Arrange
        product.setActive(false);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        // Act
        productService.activate(productId);

        // Assert
        assertEquals(true, product.isActive());
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when activating non-existent product")
    void activate_WhenDoesNotExist_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.activate(productId));
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(product);
    }

    @Test
    @DisplayName("Should deactivate product when exists")
    void deactivate_WhenExists_ShouldSetActiveFalse() {
        // Arrange
        product.setActive(true);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        // Act
        productService.deactivate(productId);

        // Assert
        assertEquals(false, product.isActive());
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deactivating non-existent product")
    void deactivate_WhenDoesNotExist_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.deactivate(productId));
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(product);
    }

    @Test
    @DisplayName("Should add image to product when image exists in storage")
    void addImageToProduct_WhenImageExists_ShouldSaveAndReturnResponse() {
        // Arrange
        String objectKey = "image2.jpg";
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(storageService.exists(objectKey)).thenReturn(true);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDTO(product)).thenReturn(productResponseDTO);
        when(storageService.getUrl("image1.jpg")).thenReturn("http://example.com/image1.jpg");
        when(storageService.getUrl("image2.jpg")).thenReturn("http://example.com/image2.jpg");

        // Act
        ProductResponseDTO result = productService.addImageToProduct(productId, objectKey);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getImageUrls().size());
        assertEquals("http://example.com/image2.jpg", result.getImageUrls().get(1));
        verify(productRepository, times(1)).findById(productId);
        verify(storageService, times(1)).exists(objectKey);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when image is missing in storage")
    void addImageToProduct_WhenImageMissing_ShouldThrowException() {
        // Arrange
        String objectKey = "missing-image.jpg";
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(storageService.exists(objectKey)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> productService.addImageToProduct(productId, objectKey));
        verify(productRepository, times(1)).findById(productId);
        verify(storageService, times(1)).exists(objectKey);
        verify(productRepository, never()).save(product);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when adding image to non-existent product")
    void addImageToProduct_WhenProductDoesNotExist_ShouldThrowException() {
        // Arrange
        String objectKey = "image2.jpg";
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> productService.addImageToProduct(productId, objectKey));
        verify(productRepository, times(1)).findById(productId);
        verify(storageService, never()).exists(objectKey);
    }
}
